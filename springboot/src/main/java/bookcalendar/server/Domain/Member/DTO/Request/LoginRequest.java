package bookcalendar.server.Domain.Member.DTO.Request;

public record LoginRequest(
        String nickName,
        String password
) {
}
