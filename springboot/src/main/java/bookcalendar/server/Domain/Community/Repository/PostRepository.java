package bookcalendar.server.Domain.Community.Repository;

import bookcalendar.server.Domain.Community.DTO.Response.PostListResponse;
import bookcalendar.server.Domain.Community.DTO.Response.PostResponse;
import bookcalendar.server.Domain.Community.Entity.Post;
import bookcalendar.server.Domain.Community.Entity.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Integer> {

        /**
         * 게시글 고유 번호를 통한 게시글 조회
         *
         * @param postId 게시글 고유 번호
         * @return 게시글 객체
         */
        Optional<Post> findByPostId(Integer postId);

        /**
         * 게시글 리스트 반환
         *
         * @return 게시글 리스트
         */
        @Query("SELECT new bookcalendar.server.Domain.Community.DTO.Response.PostListResponse(p.postId, p.title, m.nickName, p.date) "
                        +
                        "FROM Post p JOIN p.member m")
        List<PostListResponse> findAllPostSummaries();

        /**
         * 게시글 정보 반환
         *
         * @param postId 조회하고자 하는 게시글 고유 번호
         * @return 게시글 정보
         */
        @Query("SELECT new bookcalendar.server.Domain.Community.DTO.Response.PostResponse(" +
                        "p.postId, m.memberId, m.nickName, p.title, p.contents) " +
                        "FROM Post p JOIN p.member m WHERE p.postId = :postId")
        Optional<PostResponse> getPostDetail(@Param("postId") Integer postId);


        /**
         * 키워드로 게시글 검색
         *
         * @param keyword 검색 키워드
         * @return 게시글 리스트
         */
        @Query("SELECT new bookcalendar.server.Domain.Community.DTO.Response.PostListResponse(p.postId, p.title, m.nickName, p.date) "
                        +
                        "FROM Post p JOIN p.member m WHERE p.title LIKE %:keyword%")
        List<PostListResponse> searchPostsByKeyword(@Param("keyword") String keyword);



}
