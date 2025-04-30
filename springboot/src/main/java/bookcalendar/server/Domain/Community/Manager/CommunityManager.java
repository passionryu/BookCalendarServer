package bookcalendar.server.Domain.Community.Manager;

import bookcalendar.server.Domain.Community.Entity.Comment;
import bookcalendar.server.Domain.Community.Entity.Post;
import bookcalendar.server.Domain.Community.Exception.CommunityException;
import bookcalendar.server.Domain.Community.Repository.CommentRepository;
import bookcalendar.server.Domain.Community.Repository.PostRepository;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Exception.MemberException;
import bookcalendar.server.Domain.Member.Repository.MemberRepository;
import bookcalendar.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
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
     * 댓글 객체 반환 메서드
     *
     * @param commentId 댓글 고유 번호
     * @return 댓글 객체
     */
    public Comment getComment(Integer commentId){
        return commentRepository.findByCommentId(commentId)
                .orElseThrow(()-> new CommunityException(ErrorCode.COMMENT_NOT_FOUND));
    }

    // TODO : 스케줄러를 사용하는 것이 좋을까 아니면 독후감을 작성할때마다 리셋 하는 것이 좋을까....
    /**
     * 랭킹 재배치 스케줄러
     */
    @Scheduled(cron = "0 0/10 * * * ?")  // 매 10분마다 실행
    @Transactional
    @CacheEvict(value = "rankCache", allEntries = true)
    public void recalculateAllRanks() {
        log.info("✅ 랭킹 재계산 작업 시작");

        // 리뷰 수를 기준으로 내림차순으로 모든 멤버를 가져옴
        List<Member> members = memberRepository.findAll(Sort.by(Sort.Direction.DESC, "reviewCount"));
        int total = members.size();

        // 각 멤버의 랭킹을 계산
        for (int i = 0; i < total; i++) {
            Member m = members.get(i);
            int percentile = (int) Math.floor((double) i / total * 100);
            m.setRank(percentile);  // 각 멤버의 랭킹을 설정
        }

        log.info("✅ 랭킹 재계산 작업 완료");
    }

}
