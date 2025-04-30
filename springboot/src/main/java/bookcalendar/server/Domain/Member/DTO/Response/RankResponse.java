package bookcalendar.server.Domain.Member.DTO.Response;

public record RankResponse(
        String nickName,
        Integer rank,
        Integer reviewCount
) {
}
