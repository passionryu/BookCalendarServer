package bookcalendar.server.Domain.Community.Service;

import bookcalendar.server.Domain.Community.DTO.Request.PostRequest;
import bookcalendar.server.Domain.Community.Entity.Post;
import bookcalendar.server.Domain.Community.Helper.CommunityHelper;
import bookcalendar.server.Domain.Community.Manager.CommunityManager;
import bookcalendar.server.Domain.Community.Repository.PostRepository;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Exception.MemberException;
import bookcalendar.server.Domain.Member.Repository.MemberRepository;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final PostRepository postRepository;
    private final CommunityManager communityManager;

    /**
     * 게시글 작성 메서드
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param postRequest 포스팅할 게시글 정보 DTO
     */
    @Override
    @Transactional
    public Integer writePost(CustomUserDetails customUserDetails, PostRequest postRequest) {

        // 현재 멤버 객체 반환
        Member member = communityManager.getMember(customUserDetails.getMemberId());

        // 입력 정보 및 유저 정보를 통해 Post 엔티티 생성
        Post post = postRepository.save(CommunityHelper.postEntityBuilder(member, postRequest));
        return post.getPostId();
    }

    /**
     * 게시글 삭제 메서드
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param postId 삭제할 게시글 고유 번호
     */
    @Override
    public void deletePost(CustomUserDetails customUserDetails, Integer postId) {

        // 게시글 ID를 통한 게시글 객체 반환
        Post post = communityManager.getPost(postId);

        // 삭제 권한이 있는지 확인 - 본인 인증
        CommunityHelper.checkOwnership(customUserDetails, post);

        // 해당 post 객체 삭제
        postRepository.delete(post);
    }
}
