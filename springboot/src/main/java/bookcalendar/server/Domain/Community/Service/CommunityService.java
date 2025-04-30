package bookcalendar.server.Domain.Community.Service;

import bookcalendar.server.Domain.Community.DTO.Request.CommentRequest;
import bookcalendar.server.Domain.Community.DTO.Request.PostRequest;
import bookcalendar.server.Domain.Community.DTO.Response.CommentResponse;
import bookcalendar.server.Domain.Community.DTO.Response.PostListResponse;
import bookcalendar.server.Domain.Community.DTO.Response.PostResponse;
import bookcalendar.server.Domain.Member.DTO.Response.RankResponse;
import bookcalendar.server.global.Security.CustomUserDetails;

import java.util.List;

public interface CommunityService {

    /**
     * 게시글 작성 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param postRequest 포스팅 할 게시글 정보 DTO
     */
    Integer writePost(CustomUserDetails customUserDetails, PostRequest postRequest);

    /**
     * 게시글 삭제 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param postId 게시글의 고유 번호
     */
    void deletePost(CustomUserDetails customUserDetails, Integer postId);

    /**
     * 유저 메달 및 랭킹 반환 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 유저 메달 및 랭킹 반환
     */
    RankResponse getRank(CustomUserDetails customUserDetails);

    /**
     * 커뮤니티 게시글 리스트 반환 인터페이스
     *
     * @return 커뮤니티 게시글 리스트
     */
    List<PostListResponse> getPostList();

    /**
     * 커뮤니티에서 게시글 반환 인터페이스
     *
     * @param postId 게시글 고유 번호
     * @return 게시글 정보
     */
    PostResponse getPostDetail(Integer postId);

    /**
     * 댓글 작성 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param postId 댓글이 달리는 게시글 고유 번호
     * @param commentRequest 댓글 요청 데이터
     * @return 작성된 데이터 객체의 고유 번호
     */
    void createComment(CustomUserDetails customUserDetails, Integer postId, CommentRequest commentRequest);

    /**
     * 게시물에서 댓글 리스트 조회 인터페이스
     *
     * @param postId 댓글이 달린 게시물 고유 번호
     * @return 해당 게시글의 댓글 리스트
     */
    List<CommentResponse> getCommentList(Integer postId);

    /**
     * 내 댓글 삭제 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param commentId 삭제할 댓글 고유 번호
     */
    void deleteComment(CustomUserDetails customUserDetails, Integer commentId);

    /**
     * 게시글 작성자의 본인 게시글 내의 댓글 삭제 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param postId 삭제할 댓글의 게시글 고유 번호
     * @param commentId 삭제할 댓글의 고유 번호
     */
    void deleteCommentByPostOwner(CustomUserDetails customUserDetails, Integer postId, Integer commentId);
}
