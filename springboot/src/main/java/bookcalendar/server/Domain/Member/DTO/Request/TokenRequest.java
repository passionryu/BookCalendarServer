package bookcalendar.server.Domain.Member.DTO.Request;

public record TokenRequest(
        String accessToken,
        String refreshToken
) {
}
