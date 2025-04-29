package bookcalendar.server.Domain.Community.Service;

import bookcalendar.server.Domain.Community.DTO.Request.PostRequest;
import bookcalendar.server.Domain.Community.Entity.Post;
import bookcalendar.server.global.Security.CustomUserDetails;

public interface CommunityService {

    /**
     * 게시글 작성 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param postRequest 포스팅 할 게시글 정보 DTO
     */
    Integer writePost(CustomUserDetails customUserDetails, PostRequest postRequest);

}
