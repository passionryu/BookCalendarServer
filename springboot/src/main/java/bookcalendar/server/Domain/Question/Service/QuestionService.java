package bookcalendar.server.Domain.Question.Service;

import bookcalendar.server.Domain.Question.DTO.Request.QuestionAnswerRequest;
import bookcalendar.server.Domain.Review.DTO.Response.AiResponse;
import bookcalendar.server.global.Security.CustomUserDetails;

public interface QuestionService {

    /**
     * 질문지 작성 인터페이스
     *
     * @param customUserDetails
     * @param questionAnswerRequest
     * @return
     */
    AiResponse writeQuestion(CustomUserDetails customUserDetails, QuestionAnswerRequest questionAnswerRequest);

}
