package bookcalendar.server.Domain.Community.Repository;

import bookcalendar.server.Domain.Community.Entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post,Integer> {

    /**
     * 게시글 고유 번호를 통한 게시글 조회
     *
     * @param postId 게시글 고유 번호
     * @return 게시글 객체
     */
    Optional<Post> findByPostId(Integer postId);
}
