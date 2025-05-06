package bookcalendar.server.Domain.Mypage.DTO.Response;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record UserAllInfoResponse(
        String nickName,
        String phoneNumber,
        String genre,
        String job,
        LocalDate birth
) {
}
