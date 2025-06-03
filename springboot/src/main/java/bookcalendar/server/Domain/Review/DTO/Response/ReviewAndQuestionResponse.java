package bookcalendar.server.Domain.Review.DTO.Response;

import bookcalendar.server.Domain.Question.Entity.Question;
import bookcalendar.server.Domain.Review.Entity.Review;

public record ReviewAndQuestionResponse(
        Review review,
        Question question
) {}
