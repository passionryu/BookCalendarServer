package bookcalendar.server.Domain.Review.Manager;

import bookcalendar.server.Domain.Book.Entity.Book;
import bookcalendar.server.Domain.Book.Repository.BookRepository;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Exception.MemberException;
import bookcalendar.server.Domain.Member.Repository.MemberRepository;
import bookcalendar.server.Domain.MockAiModel.Emotion.EmotionMockModel;
import bookcalendar.server.Domain.MockAiModel.Question.QuestionMockModel;
import bookcalendar.server.Domain.Question.Entity.Question;
import bookcalendar.server.Domain.Question.Exception.QuestionException;
import bookcalendar.server.Domain.Question.Repository.QuestionRepository;
import bookcalendar.server.Domain.Review.DTO.Response.EmotionAiResponse;
import bookcalendar.server.Domain.Review.DTO.Response.ProgressResponse;
import bookcalendar.server.Domain.Review.DTO.Response.QuestionNumberTwoThreeResponse;
import bookcalendar.server.Domain.Review.DTO.Response.ReviewAndQuestionResponse;
import bookcalendar.server.Domain.Review.Entity.Review;
import bookcalendar.server.Domain.Review.Helper.ReviewHelper;
import bookcalendar.server.Domain.Review.Repository.ReviewRepository;
import bookcalendar.server.Domain.Review.ReviewException;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewManager {

    @Qualifier("cacheRedisTemplate")
    private final RedisTemplate<String, String> redisTemplate;

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final QuestionRepository questionRepository;
    private final ChatClient chatClient;

    private final CacheManager cacheManager;

    /* Gpt Mock AI 모델 */
    private final EmotionMockModel emotionMockModel;
    private final QuestionMockModel questionMockModel;

    // ======================= Util 영역 =========================

    /* 유저 본인이 "독서중"인 도서를 반환하는 메서드 */
    public Book getBook_UserReading(Integer memberId){
        return bookRepository.findByMemberIdAndStatus(memberId, Book.Status.독서중)
                .orElseThrow(()->new MemberException(ErrorCode.BOOK_NOT_FOUND) );
    }

    // ======================= 독후감 작성 영역 =========================

    /**
     * 감정 분석 AI 모델 호출 메서드
     *
     * @param contents 유저의 독후감
     * @return 감정, 1번 질문지 DTO
     */
    public EmotionAiResponse requestEmotionAi(String contents){

        /* 로컬 용 Mock AI 모델 호출 */
        String emotion = emotionMockModel.numberOneQuestion(contents);

        /* Fast-API 의 감정 분류 AI 모델 호출 */
        //String emotion = emotionClient.predict(contents).block();

        /* 1번 질문지 생성 */
        String question1 = ReviewHelper.makeQuestion1(emotion);

        return new EmotionAiResponse(emotion, question1);
    }

    /**
     * 질문지 생성 AI 모델 호출 메서드
     *
     * @param contents 유저의 독후감
     * @return 2,3번 질문지 DTO
     */
    public QuestionNumberTwoThreeResponse requestQuestionAi(String contents){

        /* 로컬 용 Mock AI 모델 호출 */
        QuestionNumberTwoThreeResponse questionNumberTwoThreeResponse = questionMockModel.numberTwoThreeQuestion(contents);

        /* Fast-API 의 질문지 생성 AI 모델 호출 */
        // QuestionNumberTwoThreeResponse questionNumberTwoThreeResponse = questionClient.predict(contents).block();

        return questionNumberTwoThreeResponse;
    }

    /**
     * 독후감 제출시 DB 작업 메서드
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param contents 독후감 본문
     * @param progressResponse 진행률
     * @param pages 오늘 독서한 페이지 수
     * @param emotion 유저의 감정
     * @param member 멤버 객체
     * @param book 도서 객체
     * @param question1 1번 질문
     * @param questionNumberTwoThreeResponse 2,3번 질문 DTO
     * @return Review,Question 객체 DTO
     */
    public ReviewAndQuestionResponse processReviewSubmission(CustomUserDetails customUserDetails, String contents, ProgressResponse progressResponse,
                                                             Integer pages, String emotion, Member member,
                                                             Book book, String question1, QuestionNumberTwoThreeResponse questionNumberTwoThreeResponse) {

        /* 결과 Review DB에 저장 하는 로직 */
        Review savedReview = saveReview(
                contents, progressResponse, pages,
                emotion, member, book
        );

        member.setReviewCount(member.getReviewCount() + 1); // review저장 후 member의 reviewCount에 +1
        deleteMonthlyReviewListCache(customUserDetails); // monthlyReviewList 캐시 삭제

        // 이벤트 큐에 유저 고유 번호를 저장
        redisTemplate.opsForSet().add("ranking:update:memberIds", member.getMemberId().toString());

        /* 결과를 Question DB에 저장하는 로직 */
        Question question = saveQuestion(
                savedReview,
                member,
                question1,
                questionNumberTwoThreeResponse.question1(),
                questionNumberTwoThreeResponse.question2()
        );

        return new ReviewAndQuestionResponse(savedReview, question);
    }

    /**
     * 독후감을 작성하기 전에 오늘 작성한 독후감이 있는지 Check하는 메서드
     *
     * @param memberId 유저의 고유 번호
     */
    public void check_today_review(Integer memberId){
        if(reviewRepository.existsByMember_MemberIdAndDate(memberId, LocalDate.now()))
            throw new ReviewException(ErrorCode.ALREADY_EXIST_REVIEW);
    }

    /**
     * 유저 고유 번호를 통한 멤버 객체 반환 메서드
     *
     * @param memberId 유저의 고유 번호
     * @return 유저 객체
     */
    public Member getMember(Integer memberId){
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(()->new MemberException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 진행률 & 기존 독서 페이지 반환 메서드
     *
     * @param memberId 유저의 고유 번호
     * @param pages 오늘 읽은 독서 페이지
     * @return 진행률 & 기존 독서 페이지 DTO
     */
    public ProgressResponse getProgress(Integer memberId, Integer pages){

        Member member = getMember(memberId);
        Book book = getBook_UserReading(memberId);

        // 특정 멤버와 책에 대한 기존 독후감들의 총 페이지 수 계산
        List<Review> reviews = reviewRepository.findByMember_MemberIdAndBook_BookId(
                member.getMemberId(),
                book.getBookId()
        );

        // 기존의 모든 독서 페이지 종합
        int previousPages = reviews.stream()
                .mapToInt(Review::getPages)
                .sum();

        // 이 함수는 진행률이 100이 넘어갈 위험이 있다.
        // Integer progress = (int)(((double) (previousPages + pages) / book.getTotalPage()) * 100);
        // Math.min(100, )함수를 활용하여 진행률이 100이상일 지라도 100으로 제한
        Integer progress = Math.min(100, (int)(((double)(previousPages + pages) / book.getTotalPage()) * 100));

        return new ProgressResponse(progress,previousPages);

    }

    /**
     * 독후감 객체를 DB에 저장하는 메서드
     *
     * @param contents 독후감 본문
     * @param progress 진행률
     * @param todayPages 오늘 독서한 페이지
     * @param emotion 독후감 내부의 감정
     * @param member 유저 객체
     * @param book 독서 객체
     * @return 독후감 객체
     */
    public Review saveReview(String contents, ProgressResponse progress, int todayPages,
                             String emotion, Member member, Book book) {
        return reviewRepository.save(Review.builder()
                .contents(contents)
                .progress(progress.progress())
                .pages(progress.previousPages() + todayPages)
                .emotion(emotion)
                .member(member)
                .book(book)
                .date(LocalDate.now())
                .build());
    }

    /**
     * 질문 객체를 DB에 저장하는 메서드
     *
     * @param review 독후감 객체
     * @param member 유저 객체
     * @param q1 질문지
     * @param q2 질문지
     * @param q3 질문지
     * @return 질문지 객체
     */
    public Question saveQuestion(Review review, Member member,
                                 String q1, String q2, String q3) {
        return questionRepository.save(Question.builder()
                .review(review)
                .member(member)
                .question1(q1)
                .question2(q2)
                .question3(q3)
                .feedback1(0)
                .feedback2(0)
                .feedback3(0)
                .build());
    }

    /**
     * MonthlyReviewList 캐시 삭제 메서드
     *
     * @param customUserDetails 인증된 유저 객체
     */
    public void deleteMonthlyReviewListCache(CustomUserDetails customUserDetails){

        Cache cache = cacheManager.getCache("monthlyReviewList");
        if (cache != null) {
            for (int month = 1; month <= 12; month++) {
                String key = customUserDetails.getMemberId() + "-" + month;
                cache.evict(key); // Spring 내부 키 전략을 그대로 따름
            }
        }
    }

    // ======================= 캘린더에서 날짜 선택 후 독후감 기록 조회 영역 =========================

    /* 캘린더에서 날짜 선택 시 해당 날짜의 독후감 조회 */
    public Review getReviewByDate(Integer memberId, LocalDate date ) {
        return reviewRepository.findByMember_MemberIdAndDate(memberId, date)
                .orElseThrow(()-> new ReviewException(ErrorCode.REVIEW_NOT_FOUND));
    }

    /* reviewId로 question객체 반환 */
    public Question getQuestionByReviewId(Integer reviewId) {
        return questionRepository.findByReview_ReviewId(reviewId)
                .orElseThrow(()->new QuestionException(ErrorCode.QUESTION_NOT_FOUND));
    }


}
