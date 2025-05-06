package bookcalendar.server.Domain.Mypage.DTO.Response;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record UserInfoEditResponse(
        String nickName,
        String phoneNumber,
        String genre,
        String job,
        LocalDate birth
) {
}
