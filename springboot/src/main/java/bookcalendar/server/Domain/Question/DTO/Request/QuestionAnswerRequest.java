package bookcalendar.server.Domain.Question.DTO.Request;

public record QuestionAnswerRequest(
        Integer questionId,
        String answer1,
        String answer2,
        String answer3,
        Integer feedback1,
        Integer feedback2,
        Integer feedback3
) {}
