package bookcalendar.server.Domain.Community.Repository;

import bookcalendar.server.Domain.Community.Entity.Post;
import bookcalendar.server.Domain.Community.Entity.PostReport;
import bookcalendar.server.Domain.Member.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostReportRepository extends JpaRepository<PostReport,Integer> {

    /**
     * 현재 유저가 해당 게시글에 신고버튼을 누른 이력이 있는지 확인
     *
     * @param post 신고하고자 하는 게시글 객체
     * @param member 신고하는 유저 객체
     * @return 참 거짓
     */
    boolean existsByPostAndMember(Post post, Member member);
}
