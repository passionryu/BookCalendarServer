package bookcalendar.server.Domain.Mypage.Manager;

import bookcalendar.server.Domain.Book.Repository.BookRepository;
import bookcalendar.server.Domain.Community.Entity.Comment;
import bookcalendar.server.Domain.Community.Entity.Post;
import bookcalendar.server.Domain.Community.Entity.Scrap;
import bookcalendar.server.Domain.Community.Exception.CommunityException;
import bookcalendar.server.Domain.Community.Repository.CommentRepository;
import bookcalendar.server.Domain.Community.Repository.PostRepository;
import bookcalendar.server.Domain.Community.Repository.ScrapRepository;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Exception.MemberException;
import bookcalendar.server.Domain.Member.Repository.MemberRepository;
import bookcalendar.server.Domain.Question.Entity.Question;
import bookcalendar.server.Domain.Question.Repository.QuestionRepository;
import bookcalendar.server.Domain.Review.Entity.Review;
import bookcalendar.server.Domain.Review.Repository.ReviewRepository;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MypageManager {

    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final QuestionRepository questionRepository;
    private final ReviewRepository reviewRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ScrapRepository scrapRepository;

    /**
     * 멤버 객체 반환 메서드
     *
     * @param memberId 유저의 고유 번호
     * @return 멤버 객체
     */
    public Member getMember(Integer memberId) {
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 독후감 객체 반환 메서드
     *
     * @param reviewId 독후감 고유 번호
     * @return 독후감 객체
     */
    public Review getReview(Integer reviewId){

        return reviewRepository.findByReviewId(reviewId)
                .orElseThrow(()-> new CommunityException(ErrorCode.REVIEW_NOT_FOUND));
    }

    /**
     * 멤버 고유 번호를 통한 독후감 객체 반환 메서드
     *
     * @param memberId 유저의 고유 번호
     * @return 도서 객체
     */
    public List<Review> getReviewListByMemberId(Integer memberId){

        return reviewRepository.findByMember_MemberId(memberId);
    }

    /**
     * 독후감 고유 번호를 통해 질문 객체 반환
     *
     * @param reviewId 독후감 고유 번호
     * @return 질문 객체
     */
    public Question getQuestion(Integer reviewId){
        return questionRepository.findByReview_ReviewId(reviewId)
                .orElseThrow(()-> new CommunityException(ErrorCode.QUESTION_NOT_FOUND));
    }

    /**
     * 유저의 고유 번호로 scrap리스트 반환
     *
     * @param memberId 유저의 고유 번호
     * @return 스크랩 리스트 반환
     */
    public List<Scrap> getScrapListByMemberId(Integer memberId){
        return scrapRepository.findByMember_MemberId(memberId);
    }

    /**
     * 스크랩 고유 번호를 통한 게시글 반환
     *
     * @param scrapId 스크랩 고유 번호
     * @return 게시글 객체
     */
    public Post getPostByScrapId(CustomUserDetails customUserDetails, Integer scrapId){

        Scrap scrap = scrapRepository.findByScrapId(scrapId)
                .orElseThrow(()->new CommunityException(ErrorCode.POST_NOT_FOUND));

        return scrap.getPost();
    }

    /**
     * 스크랩 고유 번호를 통해 스크랩 객체 반환
     *
     * @param scrapId 찾고자 하는 스크랩 고유 번호
     * @return 스크랩 객체
     */
    public Scrap getScrapByScrapId(Integer scrapId){

        return scrapRepository.findByScrapId(scrapId)
                .orElseThrow(()->new CommunityException(ErrorCode.POST_NOT_FOUND));
    }

}
