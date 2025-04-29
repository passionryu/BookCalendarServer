package bookcalendar.server.Domain.Community.Helper;

import bookcalendar.server.Domain.Community.DTO.Request.PostRequest;
import bookcalendar.server.Domain.Community.Entity.Post;
import bookcalendar.server.Domain.Member.Entity.Member;

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
}
