package bookcalendar.server.Domain.Review.DTO.Response;

public record QuestionResponse(
        Integer reviewId,
        Integer questionId,
        String question1,
        String question2,
        String question3
) {
}
