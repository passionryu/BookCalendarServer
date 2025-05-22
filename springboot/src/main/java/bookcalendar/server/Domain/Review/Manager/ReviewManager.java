package bookcalendar.server.Domain.Review.Manager;

import bookcalendar.server.Domain.Book.Entity.Book;
import bookcalendar.server.Domain.Book.Repository.BookRepository;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Exception.MemberException;
import bookcalendar.server.Domain.Member.Repository.MemberRepository;
import bookcalendar.server.Domain.Question.Entity.Question;
import bookcalendar.server.Domain.Question.Exception.QuestionException;
import bookcalendar.server.Domain.Question.Repository.QuestionRepository;
import bookcalendar.server.Domain.Review.DTO.Response.ProgressResponse;
import bookcalendar.server.Domain.Review.Entity.Review;
import bookcalendar.server.Domain.Review.Repository.ReviewRepository;
import bookcalendar.server.Domain.Review.ReviewException;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

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

    // ======================= Util 영역 =========================

    /* 유저 본인이 "독서중"인 도서를 반환하는 메서드 */
    public Book getBook_UserReading(Integer memberId){
        return bookRepository.findByMemberIdAndStatus(memberId, Book.Status.독서중)
                .orElseThrow(()->new MemberException(ErrorCode.BOOK_NOT_FOUND) );
    }

    // ======================= 독후감 작성 영역 =========================

    /* 독후감을 작성하기 전에 오늘 작성한 독후감이 있는지 Check하는 메서드 */
    public void check_today_review(Integer memberId){
        if(reviewRepository.existsByMember_MemberIdAndDate(memberId, LocalDate.now()))
            throw new ReviewException(ErrorCode.ALREADY_EXIST_REVIEW);
    }

    /* 유저 고유 번호를 통한 멤버 객체 반환 메서드 */
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

        // Progress 계산 로직
        Integer progress = (int)(((double) (previousPages + pages) / book.getTotalPage()) * 100);

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

        Set<String> keys = redisTemplate.keys("monthlyReviewList::" + customUserDetails.getMemberId() + "-*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
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
