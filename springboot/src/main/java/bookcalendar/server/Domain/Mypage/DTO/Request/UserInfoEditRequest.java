package bookcalendar.server.Domain.Mypage.DTO.Request;

import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record UserInfoEditRequest(

        String nickName,
        @Pattern(regexp = "^(\\d{3})-(\\d{4})-(\\d{4})$", message = "전화번호는 000-0000-0000 형식으로 입력해야 합니다.")
        String phoneNumber,
        String genre,
        String job,
        LocalDate birth
) {
}
