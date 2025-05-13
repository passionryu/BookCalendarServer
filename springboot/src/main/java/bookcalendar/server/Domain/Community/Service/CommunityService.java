package bookcalendar.server.Domain.Community.Service;

import bookcalendar.server.Domain.Community.DTO.Request.CommentRequest;
import bookcalendar.server.Domain.Community.DTO.Request.PostRequest;
import bookcalendar.server.Domain.Community.DTO.Response.CommentResponse;
import bookcalendar.server.Domain.Community.DTO.Response.PostListResponse;
import bookcalendar.server.Domain.Community.DTO.Response.PostResponse;
import bookcalendar.server.Domain.Community.DTO.Response.TopLikedPosts;
import bookcalendar.server.Domain.Member.DTO.Response.RankResponse;
import bookcalendar.server.global.Security.CustomUserDetails;
import org.springframework.web.bind.annotation.RequestParam;

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
    PostResponse getPostDetail(CustomUserDetails customUserDetails,Integer postId);

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

    /**
     * 게시글 신고 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param postId 신고하고자 하는 게시글 고유 번호
     */
    void reportPost(CustomUserDetails customUserDetails, Integer postId);

    /**
     * 댓글 신고 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param commentId 신고하고자 하는 댓글의 고유 번호
     */
    void reportComment(CustomUserDetails customUserDetails, Integer commentId);

    /**
     * 게시글 스크랩 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param postId 스크랩 하고자 하는 게시글의 고유 번호
     */
    void scrapPost(CustomUserDetails customUserDetails, Integer postId);

    /**
     * 게시글 검색 인터페이스
     *
     * @param keyword 검색 키워드
     * @return 검색된 게시글 리스트
     */
    List<PostListResponse> searchPost(String keyword);

    /**
     * Like 버튼 누르기 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param postId 게시글 고유 번호
     * @return 좋아요 총 합산 수
     */
    Integer clickLike(CustomUserDetails customUserDetails, Integer postId);

    /**
     * Like 카운트 총 합산 반환 인터페이스
     *
     * @param postId 게시글 고유 번호
     * @return 좋아요 총 합산 수
     */
    Integer getLikeCount(Integer postId);

    /**
     * Like 수 Top3 게시글 썸네일 리스트 반환 인터페이스
     *
     * @return Like 수 Top3 게시글 썸네일 리스트
     */
    List<TopLikedPosts> getTopLikedPosts();

}
