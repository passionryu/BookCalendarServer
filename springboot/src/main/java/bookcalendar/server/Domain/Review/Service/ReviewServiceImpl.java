package bookcalendar.server.Domain.Review.Service;

import bookcalendar.server.Domain.Book.Entity.Book;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.MockAiModel.Emotion.EmotionMockModel;
import bookcalendar.server.Domain.MockAiModel.Question.QuestionMockModel;
import bookcalendar.server.Domain.Question.Entity.Question;
import bookcalendar.server.Domain.Review.DTO.Request.ReviewRequest;
import bookcalendar.server.Domain.Review.DTO.Response.*;
import bookcalendar.server.Domain.Review.Entity.Review;
import bookcalendar.server.Domain.Review.Helper.ReviewHelper;
import bookcalendar.server.Domain.Review.Manager.ReviewManager;
import bookcalendar.server.Domain.Review.Repository.ReviewRepository;
import bookcalendar.server.global.ExternalConnection.Client.EmotionClient;
import bookcalendar.server.global.ExternalConnection.Client.QuestionClient;
import bookcalendar.server.global.Security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    @Qualifier("cacheRedisTemplate")
    private final RedisTemplate<String, String> redisTemplate;

    private final ReviewManager reviewManager;
    private final ReviewRepository reviewRepository;

    /* AI 모델 커넥션 영역 */
    private final EmotionClient emotionClient;
    private final QuestionClient questionClient;

    /* Gpt Mock AI 모델 */
    private final EmotionMockModel emotionMockModel;
    private final QuestionMockModel questionMockModel;

    // ======================= 독후감 작성 영역 =========================

    /* 독후감 작성 메서드 */
    @Override
    @Transactional
    @CacheEvict(value = "mainPageResponse", key = "#customUserDetails.memberId")
    @Caching(evict = {
            @CacheEvict(value = "mainPageResponse", key = "#customUserDetails.memberId"),
            @CacheEvict(value = "myReviewList", key = "#customUserDetails.memberId"),
            @CacheEvict(value = "myStatistics", key = "#customUserDetails.memberId")
    })
    public QuestionResponse writeReview(CustomUserDetails customUserDetails, ReviewRequest reviewRequest) {

        String contents = reviewRequest.contents(); // 오늘 독후감 본문
        Integer pages = reviewRequest.pages(); // 오늘 독서한 페이지 수
        Book book = reviewManager.getBook_UserReading(customUserDetails.getMemberId()); // 현재 유저의 독서중인 도서 객체 반환
        Member member = reviewManager.getMember(customUserDetails.getMemberId()); // 현재 유저의 멤버 객체 반환

        // 오늘 독후감이 존재하는지 확인
        reviewManager.check_today_review(customUserDetails.getMemberId());

        // (진행률 + 기존 독서 페이지) DTO 반환 메서드
        ProgressResponse progressResponse = reviewManager.getProgress(customUserDetails.getMemberId(), pages);

        /* 감정 분석 AI 모델로 요청 */
        String emotion = emotionMockModel.numberOneQuestion(contents); // 로컬 용 Mock AI 모델 호출
        //String emotion = emotionClient.predict(contents).block(); // Fast-API 의 감정 분류 AI 모델 호출
        String question1 = ReviewHelper.makeQuestion1(emotion); // 1번 질문지 생성

        /* 2번 질문지 생성 AI 모델로 요청 */
        QuestionNumberTwoThreeResponse questionNumberTwoThreeResponse = questionMockModel.numberTwoThreeQuestion(contents); //로컬 용 Mock AI 모델 호출
        // QuestionNumberTwoThreeResponse questionNumberTwoThreeResponse = questionClient.predict(contents).block(); // Fast-API 의 질문지 생성 AI 모델 호출

        /* 결과 Review DB에 저장 하는 로직 */
        Review savedReview = reviewManager.saveReview(
                contents, progressResponse, pages,
                emotion, member, book
        );


        member.setReviewCount(member.getReviewCount() + 1); // review저장 후 member의 reviewCount에 +1
        reviewManager.deleteMonthlyReviewListCache(customUserDetails); // monthlyReviewList 캐시 삭제

        // 이벤트 큐에 유저 고유 번호를 저장
        redisTemplate.opsForSet().add("ranking:update:memberIds", member.getMemberId().toString());

        /* 결과를 Question DB에 저장하는 로직 */
        Question question = reviewManager.saveQuestion(
                savedReview,
                member,
                question1,
                questionNumberTwoThreeResponse.question1(),
                questionNumberTwoThreeResponse.question2()
        );

        return new QuestionResponse(
                savedReview.getReviewId(),
                question.getQuestionId(),
                question1,
                questionNumberTwoThreeResponse.question1(),
                questionNumberTwoThreeResponse.question2());
    }

    // ======================= 캘린더에서 날짜 선택 후 독후감 기록 조회 로직 =========================

    /* 캘린더에서 날짜 선택 후 독후감 조회 메서드 */
    @Override
    public ReviewByDateResponse getReviewByDate(CustomUserDetails customUserDetails, LocalDate date) {

        Review review = reviewManager.getReviewByDate(customUserDetails.getMemberId(), date); // 해당 날짜의 독후감 조회
        Question question = reviewManager.getQuestionByReviewId(review.getReviewId()); // reviewId로 question객체 반환

        // ReviewByDateResponse 객체 반환
        return ReviewByDateResponse.builder()
                .contents(review.getContents())
                .question1(question.getQuestion1())
                .answer1(question.getAnswer1())
                .question2(question.getQuestion2())
                .answer2(question.getAnswer2())
                .question3(question.getQuestion3())
                .answer3(question.getAnswer3())
                .aiResponse(review.getAiResponse())
                .build();
    }

    // ======================= 메인 페이지 독후감 진행률 & 남은 독서일 조회 메서드 =========================

    /* 메인 페이지 독후감 진행률 & 남은 독서일 조회 메서드 */
    @Override
    @Cacheable(value = "mainPageResponse", key = "#customUserDetails.memberId")
    public MainPageResponse mainPage(CustomUserDetails customUserDetails) {

        log.info("==> Cache Miss ( 메인 페이지 독후감 진행률 & 남은 독서일 반환): DB에서 메인 페이지 독후감 진행률 & 남은 독서일 정보를 가져옵니다.");

        Book book = reviewManager.getBook_UserReading(customUserDetails.getMemberId()); // 현재 독서중인 도서 객체 반환

        List<Review> reviews = reviewRepository.findByBook_BookId(book.getBookId()); // 도서 id로 독후감 리스트 조회
        Review latestReview = reviews.stream() // 리스트 중 가장 최근 독후감 반환
                .max(Comparator.comparing(Review::getDate)) // getDate는 LocalDate 혹은 LocalDateTime 필드
                .orElse(null); // 비어있을 경우 null 반환

        String remainDate = ReviewHelper.calculateRemainDate(book); // 독서 예정일까지 남은 날짜 계산 Helper 메서드 호출

        if(latestReview ==null)
            return new MainPageResponse(0, remainDate); // FIX: 리뷰가 없는 경우 NPE 발생 방지 (latestReview null 체크 추가)

        return new MainPageResponse(latestReview.getProgress(), remainDate);
    }

    // ======================= 캘린더에 독후감 진행률 표시 로직 =========================

    /* 캘린더에 독후감을 작성한 날짜들을 표시 메서드 */
    @Override
    @Cacheable(value = "monthlyReviewList", key = "#customUserDetails.memberId + '-' + #month")
    public List<CalendarResponse> calendar(CustomUserDetails customUserDetails, Integer month) {

        log.info("==> Cache Miss (캘린더에 독후감을 작성한 날짜 정보 반환): DB에서 캘린더에 독후감을 작성한 날짜 정보를 가져옵니다.");

        int targetMonth = (month != null) ? month : LocalDate.now().getMonthValue();
        int targetYear = LocalDate.now().getYear();

        List<Review> reviews = reviewRepository.findByMemberIdAndMonth(customUserDetails.getMemberId(), targetYear, targetMonth);

        return reviews.stream()
                .map(review -> new CalendarResponse(
                        review.getReviewId(),
                        review.getProgress(),
                        review.getDate(),
                        review.getBook().getColor()
                ))
                .toList();
    }

}
