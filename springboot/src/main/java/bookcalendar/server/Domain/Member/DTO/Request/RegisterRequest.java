package bookcalendar.server.Domain.Member.DTO.Request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record RegisterRequest(

    @NotNull(message = " 닉네임 입력은 필수입니다.")
    String nickName,

    @NotNull(message = " 비밀번호 입력은 필수입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{4,20}$", message = "비밀번호는 영문과 숫자를 포함한 4~20자리여야 합니다.")
    String password,

    @NotNull(message = "전화번호 입력은 필수입니다.")
    @Pattern(regexp = "^(\\d{3})-(\\d{4})-(\\d{4})$", message = "전화번호는 000-0000-0000 형식으로 입력해야 합니다.")
    String phoneNumber,

    @Nullable
    String genre,

    @Nullable
    String job,

    @NotNull
    LocalDate birth
) {
}
