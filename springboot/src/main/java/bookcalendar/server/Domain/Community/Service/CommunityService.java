package bookcalendar.server.Domain.Community.Service;

import bookcalendar.server.Domain.Community.DTO.Request.PostRequest;
import bookcalendar.server.Domain.Community.DTO.Response.PostListResponse;
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

}
