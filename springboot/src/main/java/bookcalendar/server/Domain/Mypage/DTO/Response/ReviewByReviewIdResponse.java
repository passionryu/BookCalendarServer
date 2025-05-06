package bookcalendar.server.Domain.Mypage.DTO.Response;

import lombok.Builder;

@Builder
public record ReviewByReviewIdResponse(
        String contents,
        String question1,
        String answer1,
        String question2,
        String answer2,
        String question3,
        String answer3,
        String aiResponse
) {
}
