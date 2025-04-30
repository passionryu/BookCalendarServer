package bookcalendar.server.Domain.Community.DTO.Response;

import java.time.LocalDateTime;

public record CommentResponse(
        Integer commentId,
        String nickName,
        Integer rank,
        Integer reviewCount,
        String contents,
        LocalDateTime date
) {}
