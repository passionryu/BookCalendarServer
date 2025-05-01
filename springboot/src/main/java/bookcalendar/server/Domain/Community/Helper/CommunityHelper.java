package bookcalendar.server.Domain.Community.Helper;

import bookcalendar.server.Domain.Community.DTO.Request.CommentRequest;
import bookcalendar.server.Domain.Community.DTO.Request.PostRequest;
import bookcalendar.server.Domain.Community.Entity.Comment;
import bookcalendar.server.Domain.Community.Entity.Post;
import bookcalendar.server.Domain.Community.Entity.Scrap;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Exception.MemberException;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.exception.ErrorCode;

import java.time.LocalDateTime;

public class CommunityHelper {

    /**
     * Post 엔티티 생성 메서드
     *
     * @param member Member 객체
     * @param postRequest 게시물 제목, 본문 포함 DTO
     * @return Post 엔티티
     *
     * date 저장은 Post 엔티티에서 prepersist로 저장
     */
    public static Post postEntityBuilder(Member member, PostRequest postRequest){
        return Post.builder()
                .member(member)
                .title(postRequest.title())
                .contents(postRequest.contents())
                .reportCount(0)
                .build();
    }

    /**
     * 댓글 객체 빌더
     *
     * @param member 댓글을 달 멤버 객체
     * @param post 댓글이 달릴 게시글 객체
     * @param commentRequest 댓글 정보
     * @return 댓글 객체
     */
    public static Comment commentEntityBuilder(Member member, Post post, CommentRequest commentRequest){
        return Comment.builder()
                .member(member)
                .post(post)
                .contents(commentRequest.contents())
                .date(LocalDateTime.now())
                .reportCount(0)
                .build();
    }

    /**
     * 스크랩 객체 빌더
     *
     * @param member 스크랩 하고자 하는 멤버 객체
     * @param post 스크랩 하고자 하는 대상 게시글
     * @return 스크랩 객체
     */
    public static Scrap scrapEntityBuilder(Member member, Post post){
        return Scrap.builder()
                .member(member)
                .post(post)
                .date(LocalDateTime.now())
                .build();
    }

    /**
     * 게시글 작성자가 현재 사용자와 동일한지 검증하는 메서드
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param post 게시글 객체
     */
    public static void checkOwnership_post(CustomUserDetails customUserDetails, Post post){
        if (!post.getMember().getMemberId().equals(customUserDetails.getMemberId())) {
            throw new MemberException(ErrorCode.NO_AUTH);
        }
    }

    /**
     * 댓글 작성자가 현재 사용자와 동일한지 검증하는 메서드
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param comment 댓글 객체
     */
    public static void checkOwnership_comment(CustomUserDetails customUserDetails, Comment comment){
        if (!comment.getMember().getMemberId().equals(customUserDetails.getMemberId())) {
            throw new MemberException(ErrorCode.NO_AUTH);
        }
    }
}
