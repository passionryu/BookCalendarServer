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

        String questionPrompt = """
            다음은 사용자가 작성한 독후감이다:
        
            %s
        
            위 독후감을 바탕으로, 사용자가 확장적 사고를 할 수 있도록 도와주는 질문을 **1개만** 생성해줘.
            - 반드시 질문은 **한 문장만** 생성해야 해.
            - 출력은 질문 문장 **하나만** 포함하고, 다른 문구나 설명은 절대 포함하지 마.
            - 질문은 1~2줄 길이의 **단일 질문**으로 구성할 것.
            """.formatted(contents);

        String question2 = chatClient.call(questionPrompt);
        String question3 = chatClient.call(questionPrompt);

        return new QuestionNumberTwoThreeResponse(question2,question3);
    }

}
