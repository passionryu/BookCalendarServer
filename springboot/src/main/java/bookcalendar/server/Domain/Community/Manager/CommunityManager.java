package bookcalendar.server.Domain.Community.Manager;

import bookcalendar.server.Domain.Community.Entity.Post;
import bookcalendar.server.Domain.Community.Exception.CommunityException;
import bookcalendar.server.Domain.Community.Repository.PostRepository;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Exception.MemberException;
import bookcalendar.server.Domain.Member.Repository.MemberRepository;
import bookcalendar.server.global.exception.ErrorCode;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityManager {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    public Member getMember(Integer memberId) {
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));
    }

    public Post getPost(Integer postId){
        return postRepository.findByPostId(postId)
                .orElseThrow(()-> new CommunityException(ErrorCode.POST_NOT_FOUND));
    }


}
