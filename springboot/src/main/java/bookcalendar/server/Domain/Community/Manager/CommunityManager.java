package bookcalendar.server.Domain.Community.Manager;

import bookcalendar.server.Domain.Community.Entity.Comment;
import bookcalendar.server.Domain.Community.Entity.Post;
import bookcalendar.server.Domain.Community.Exception.CommunityException;
import bookcalendar.server.Domain.Community.Repository.CommentRepository;
import bookcalendar.server.Domain.Community.Repository.PostRepository;
import bookcalendar.server.Domain.Community.Repository.ScrapRepository;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Exception.MemberException;
import bookcalendar.server.Domain.Member.Repository.MemberRepository;
import bookcalendar.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityManager {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    // ======================= Util 영역 =========================

    /**
     * 게시글 객체 반환 메서드
     *
     * @param postId 게시글 고유 반환
     * @return 게시글 객체
     */
    public Post getPost(Integer postId){
        return postRepository.findByPostId(postId)
                .orElseThrow(()-> new CommunityException(ErrorCode.POST_NOT_FOUND));
    }

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
     * 게시글 리스트 캐시 삭제 메서드
     *
     */
    @CachePut(value = "postList")
    public void updatePostListCache() {
        postRepository.findAllPostSummaries();
    }

    // ======================= 게시글 작성 메서드 =========================

    /**
     * 댓글 객체 반환 메서드
     *
     * @param commentId 댓글 고유 번호
     * @return 댓글 객체
     */
    public Comment getComment(Integer commentId){
        return commentRepository.findByCommentId(commentId)
                .orElseThrow(()-> new CommunityException(ErrorCode.COMMENT_NOT_FOUND));
    }

}
