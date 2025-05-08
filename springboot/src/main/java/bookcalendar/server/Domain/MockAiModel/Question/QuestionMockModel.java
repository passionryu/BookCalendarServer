package bookcalendar.server.Domain.MockAiModel.Question;

import bookcalendar.server.Domain.Review.DTO.Response.QuestionNumberTwoThreeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionMockModel {

    private final ChatClient chatClient;

    /* 2,3 번 질문지 생성 AI Mock 메서드 */
    public QuestionNumberTwoThreeResponse numberTwoThreeQuestion(String contents){

        String questionPrompt = "다음은 사용자가 작성한 독후감이다."+
                "해당 독후감에 대한서 이 독후감 작성자가 추가적인 확장적 사고를 하기 위한 AI 질문지를 1~2줄 길이로 하나 작성해줘 ->" + contents;

        String question2 = chatClient.call(questionPrompt);
        String question3 = chatClient.call(questionPrompt);

        return new QuestionNumberTwoThreeResponse(question2,question3);
    }

}
