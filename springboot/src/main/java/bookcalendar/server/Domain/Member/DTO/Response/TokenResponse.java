package bookcalendar.server.Domain.Member.DTO.Response;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
