package bookcalendar.server.Domain.Community.DTO.Response;

public record PostResponse(
        Integer postId,
        Integer memberId,
        String author,
        String title,
        String contents,
        Integer rank,
        Integer reviewCount
) { }
