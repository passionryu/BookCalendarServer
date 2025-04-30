package bookcalendar.server.Domain.Community.Repository;

import bookcalendar.server.Domain.Community.Entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRepository  extends JpaRepository<Comment,Integer> {

    /**
     * 댓글 고유 번호로 댓글 객체 반환
     *
     * @param commentId 찾고자 하는 댓글 객체의 고유 번호
     * @return 댓글 객체
     */
    Optional<Comment> findByCommentId(Integer commentId);
}
