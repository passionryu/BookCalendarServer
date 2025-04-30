package bookcalendar.server.Domain.Community.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(

        @Size(min = 1, max = 200, message = "댓글은 1자 이상 200자 이하로 작성해주세요.")
        @NotBlank(message = "댓글 내용은 비어 있을 수 없습니다.")
        String contents
) {
}
