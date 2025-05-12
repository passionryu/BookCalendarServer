package bookcalendar.server.Domain.Community.Mapper;

import bookcalendar.server.Domain.Community.DTO.Response.CommentResponse;
import bookcalendar.server.Domain.Community.DTO.Response.TopLikedPosts;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface CommunityMapper {

    /**
     * 게시글에서 댓글 리스트 반환 매퍼
     *
     * @param postId 댓글을 조회하고자 하는 게시글의 고유 번호
     * @return 댓글 리스트
     */
    List<CommentResponse> getCommentsByPostId(@Param("postId") Integer postId);

    /**
     * Like 수 Top3 게시글 썸네일 리스트 반환 매퍼
     *
     * @return Like 수 Top3 게시글 썸네일 리스트
     */
    List<TopLikedPosts> findTopLikedPosts();
}
