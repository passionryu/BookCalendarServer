package bookcalendar.server.Domain.Community.DTO.Request;

import jakarta.validation.constraints.NotBlank;

public record PostRequest(
        @NotBlank(message = "제목은 필수입니다.") String title,
        @NotBlank(message = "내용은 필수입니다.") String contents
) { }
