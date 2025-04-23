package bookcalendar.server.Domain.Review.Service;


import bookcalendar.server.Domain.Book.Entity.Book;
import bookcalendar.server.Domain.Book.Exception.BookException;
import bookcalendar.server.Domain.Book.Repository.BookRepository;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Exception.MemberException;
import bookcalendar.server.Domain.Member.Repository.MemberRepository;
import bookcalendar.server.Domain.Question.Entity.Question;
import bookcalendar.server.Domain.Question.Exception.QuestionException;
import bookcalendar.server.Domain.Question.Repository.QuestionRepository;
import bookcalendar.server.Domain.Review.DTO.Request.ReviewRequest;
import bookcalendar.server.Domain.Review.DTO.Response.*;
import bookcalendar.server.Domain.Review.Entity.Review;
import bookcalendar.server.Domain.Review.Exception.ReviewException;
import bookcalendar.server.Domain.Review.Repository.ReviewRepository;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final QuestionRepository questionRepository;
    private final ChatClient chatClient;

    /**
     * 독후감 작성 메서드
     *
     * @param customUserDetails
     * @param reviewRequest
     * @return 독후감에 대한 AI 질문 3가지
     */
    @Override
    @Transactional
    public QuestionResponse writeReview(CustomUserDetails customUserDetails, ReviewRequest reviewRequest) {

        // 사용자 요청에서 본문/오늘 독서한 페이지 수 추출
        String contents = reviewRequest.contents();
        Integer pages = reviewRequest.pages();

        // 오늘 독후감이 존재하는지 확인
        if(reviewRepository.existsByMember_MemberIdAndDate(customUserDetails.getMemberId(), LocalDate.now()))
         throw new ReviewException(ErrorCode.ALREADY_EXIST_REVIEW);

        // 현재 유저의 독서중인 도서 객체 반환
        Book book = bookRepository.findByMemberIdAndStatus( customUserDetails.getMemberId(), Book.Status.독서중)
                .orElseThrow(()->new MemberException(ErrorCode.BOOK_NOT_FOUND) );

        // 현재 유저의 멤버 객체 반환
        Member member = memberRepository.findByMemberId(customUserDetails.getMemberId())
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));

        // 특정 멤버와 책에 대한 기존 리뷰들의 총 페이지 수 계산
        List<Review> reviews = reviewRepository.findByMember_MemberIdAndBook_BookId(
                member.getMemberId(),
                book.getBookId()
        );

        int previousPages = reviews.stream()
                .mapToInt(Review::getPages)
                .sum();

        // Progress 계산 로직
        Integer progress = (int)(((double) (previousPages + pages) / book.getTotalPage()) * 100);
        log.info("previousPages : {}", previousPages);
        log.info("book.getTotalPage() : {}", book.getTotalPage());
        log.info("progress : {}", progress);

        /* 1번 질문지 생성 AI 모델로 요청 */
        QuestionNumberOneResponse questionNumberOneResponse = numberOneQuestion(contents);

        /* 2번 질문지 생성 AI 모델로 요청 */
        QuestionNumberTwoThreeResponse questionNumberTwoThreeResponse = numberTwoThreeQuestion(contents);

        /* 결과 Review DB에 저장 하는 로직 */
        Review savedReview = reviewRepository.save(Review.builder()
                        .contents(contents)
                        .progress(progress)
                        .pages(previousPages+pages)
                        .member(member)
                        .book(book)
                        .date(LocalDate.now())
                        .build());

        // reviewId 반환
        Integer reviewId = savedReview.getReviewId();

        // review저장 후 member의 reviewCount에 +1
        log.info("Before : member.getReviewCount() : {}", member.getReviewCount());
        member.setReviewCount(member.getReviewCount() + 1);
        log.info("After : member.getReviewCount() : {}", member.getReviewCount());

        /* 결과를 Question DB에 저장하는 로직 */
        Question question = questionRepository.save(Question.builder()
                        .review(savedReview)
                        .member(member)
                        .question1(questionNumberOneResponse.question1())
                        .question2(questionNumberTwoThreeResponse.question2())
                        .question3(questionNumberTwoThreeResponse.question3())
                        .feedback1(0)
                        .feedback2(0)
                        .feedback3(0)
                        .build());

        // questionId 반환
        Integer questionId = question.getQuestionId();

        return new QuestionResponse(reviewId,questionId,
                questionNumberOneResponse.question1(),
                questionNumberTwoThreeResponse.question2(),
                questionNumberTwoThreeResponse.question3());
    }


    // ======================= 캘린더에서 날짜 선택 후 독후감 기록 조회 로직 =========================

    /**
     * 캘린더에서 날짜 선택 후 독후감 조회 메서드
     *
     * @param customUserDetails
     * @param date
     * @return
     */
    @Override
    public ReviewByDateResponse getReviewByDate(CustomUserDetails customUserDetails, LocalDate date) {

        // 해당 날짜의 독후감 조회
        Review review = reviewRepository.findByMember_MemberIdAndDate(customUserDetails.getMemberId(), date)
                .orElseThrow(()->new ReviewException(ErrorCode.REVIEW_NOT_FOUND));

        // reviewId로 question객체 반환
        Question question = questionRepository.findByReview_ReviewId(review.getReviewId())
                .orElseThrow(()->new QuestionException(ErrorCode.QUESTION_NOT_FOUND));

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

    /**
     * 메인 페이지 독후감 진행률 & 남은 독서일 조회 메서드
     *
     * @param customUserDetails
     * @return
     */
    @Override
    public MainPageResponse mainPage(CustomUserDetails customUserDetails) {

        // 현재 독서중인 도서 객체 반환
        Book book = bookRepository.findByMemberIdAndStatus(customUserDetails.getMemberId(), Book.Status.독서중)
                .orElseThrow(()->new BookException(ErrorCode.BOOK_NOT_FOUND));

        // 도서 id로 독후감 리스트 조회
        List<Review> reviews = reviewRepository.findByBook_BookId(book.getBookId());

        // 리스트 중 가장 최근 독후감 반환
        Review latestReview = reviews.stream()
                .max(Comparator.comparing(Review::getDate)) // getDate는 LocalDate 혹은 LocalDateTime 필드
                .orElse(null); // 비어있을 경우 null 반환
        log.info("latestReview : {}", latestReview);
        log.info("latestReview.getProgress() : {}", latestReview.getProgress());
        Integer remainDate = Math.toIntExact(ChronoUnit.DAYS.between(LocalDate.now(), book.getFinishDate()));
        log.info("remainDate : {}", remainDate);

        return new MainPageResponse(latestReview.getProgress(), remainDate);

    }

    // ======================= 캘린더에 독후감 진행률 표시 로직 =========================

    /**
     * 캘린더에 독후감 진행률 표시 메서드
     *
     * @param customUserDetails
     * @return
     */
    @Override
    public List<CalendarResponse> calendar(CustomUserDetails customUserDetails, Integer month) {

        int targetMonth = (month != null) ? month : LocalDate.now().getMonthValue();
        int targetYear = LocalDate.now().getYear();

        List<Review> reviews = reviewRepository.findByMemberIdAndMonth(customUserDetails.getMemberId(), targetYear, targetMonth);

        return reviews.stream()
                .map(review -> new CalendarResponse(
                        review.getReviewId(),
                        review.getProgress(),
                        review.getDate()
                ))
                .toList();
    }

    // ======================= AI 모델 임시 대체 private 메서드 =========================

    /**
     * 감정 추출 후 1번 질문지 생성 모델 임시 대체 메서드
     *
     * @param contents
     * @return
     */
    private QuestionNumberOneResponse numberOneQuestion(String contents){

        String contensPrompt = "다음 글은 사용자의 독후감이다."+
                "독후감을 보고 사용자의 감정을 추출하되, (기쁨, 당황,분노, 불안, 슬픔) 중 하나로 분류해줘 ->" + contents;

        String emotion = chatClient.call(contensPrompt);
        log.info("emotion : {}", emotion);

        String questionPrompt = "다음은 사용자가 작성한 독후감에서 추출한 감정이다."+
                "이 감정값을 토대로 사용자의 감정에 관한 질문지한줄로 작성해줘." +
                " 추출된 감정 -> "+emotion;
        String question1 = chatClient.call(questionPrompt);

        return new QuestionNumberOneResponse(emotion,question1);
    }

    /**
     * 2,3번 질문지 생성에 대한 모델 임시 대체 메서드
     *
     * @param contents
     * @return
     */
    private QuestionNumberTwoThreeResponse numberTwoThreeQuestion(String contents){

        String questionPrompt = "다음은 사용자가 작성한 독후감이다."+
                "해당 독후감에 대한 사용자의 질문지를 1~2줄 길이로 하나 작성해줘 ->" + contents;

        String question2 = chatClient.call(questionPrompt);
        String question3 = chatClient.call(questionPrompt);

        return new QuestionNumberTwoThreeResponse(question2,question3);
    }


}
