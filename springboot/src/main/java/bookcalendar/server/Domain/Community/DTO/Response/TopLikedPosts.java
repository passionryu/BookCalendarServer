package bookcalendar.server.Domain.Community.DTO.Response;

import java.time.LocalDateTime;

public record TopLikedPosts(
        Integer postId,
        String title,
        String author,
        LocalDateTime date,
        Integer likeCount
) {}
