package bookcalendar.server.Domain.Community.Repository;

import bookcalendar.server.Domain.Community.Entity.Comment;
import bookcalendar.server.Domain.Community.Entity.CommentReport;
import bookcalendar.server.Domain.Member.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentReportRepository extends JpaRepository<CommentReport,Integer> {

    /**
     * 현재 유저가 해당 댓글에 신고버튼을 누른 이력이 있는지 확인
     *
     * @param comment 신고하고자 하는 댓글 객체
     * @param member 신고하는 유저 객체
     * @return 참 거짓
     */
    boolean existsByCommentAndMember(Comment comment, Member member);

}
