package bookcalendar.server.Domain.Community.Repository;

import bookcalendar.server.Domain.Community.Entity.Post;
import bookcalendar.server.Domain.Community.Entity.PostLike;
import bookcalendar.server.Domain.Member.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike,Integer> {

    /**
     * 기존에 좋아요 버튼을 누른 기록이 있는지 확인
     *
     * @param post 게시글 객체
     * @param member 유저 객체
     * @return 참 거짓
     */
    boolean existsByPostAndMember(Post post, Member member);

    /**
     * 해당 유저의 좋아요 기록 삭제
     *
     * @param post 게시글 객체
     * @param member 유저 객체
     */
    void deleteByPostAndMember(Post post,Member member);

    /**
     * 게시글에 기록된 Like 총 합산 수 반환
     *
     * @param post 게시글 객체
     * @return 게시글 Like 총 합산 수
     */
    Integer countByPost(Post post);
}
