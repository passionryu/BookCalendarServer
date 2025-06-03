package bookcalendar.server.Domain.MockAiModel.Question;

import bookcalendar.server.Domain.Review.DTO.Response.QuestionNumberTwoThreeResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionMockModel {

    private final ChatClient chatClient;


    public QuestionNumberTwoThreeResponse numberTwoThreeQuestion(String contents) {
        String questionPrompt = """
        다음은 사용자가 작성한 독후감이다:

        %s

        위 독후감을 바탕으로, 사용자가 확장적 사고를 할 수 있도록 도와주는 질문을 2개 생성해줘.
        두 질문은 서로 유사하지 않도록 해줘

        출력 형식은 반드시 아래와 같이 JSON 배열로 반환해:
        [
          "질문문장1",
          "질문문장2"
        ]

        반드시 위 JSON 포맷을 그대로 따르고, 질문 외에 절대로 아무 설명도 붙이지 마!
        """.formatted(contents);

        String jsonResponse = chatClient.call(questionPrompt);

        return parseJsonQuestions(jsonResponse);
    }

    private QuestionNumberTwoThreeResponse parseJsonQuestions(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<String> questions = objectMapper.readValue(jsonResponse, new TypeReference<List<String>>() {});

            if (questions.size() < 2) {
                log.warn("질문 수 부족: {}", jsonResponse);
                return new QuestionNumberTwoThreeResponse(
                        !questions.isEmpty() ? questions.get(0) : "질문이 생성되지 않았습니다.",
                        "질문이 생성되지 않았습니다."
                );
            }

            return new QuestionNumberTwoThreeResponse(questions.get(0), questions.get(1));
        } catch (Exception e) {
            log.error("질문 JSON 파싱 실패: {}", jsonResponse, e);
            return new QuestionNumberTwoThreeResponse("질문이 생성되지 않았습니다.", "질문이 생성되지 않았습니다.");
        }
    }


    /* 2,3 번 질문지 생성 AI Mock 메서드 ( 구버전 : 같은 프롬프트 같은 모델을 사용하기에 비슷한 질문이 두개 반환되는 경우 발생 )*/
//    public QuestionNumberTwoThreeResponse numberTwoThreeQuestion(String contents){
//
//        String questionPrompt = """
//            다음은 사용자가 작성한 독후감이다:
//
//            %s
//
//            위 독후감을 바탕으로, 사용자가 확장적 사고를 할 수 있도록 도와주는 질문을 **1개만** 생성해줘.
//            - 반드시 질문은 **한 문장만** 생성해야 해.
//            - 출력은 질문 문장 **하나만** 포함하고, 다른 문구나 설명은 절대 포함하지 마.
//            - 질문은 1~2줄 길이의 **단일 질문**으로 구성할 것.
//            """.formatted(contents);
//
//        String question2 = chatClient.call(questionPrompt);
//        String question3 = chatClient.call(questionPrompt);
//
//        return new QuestionNumberTwoThreeResponse(question2,question3);
//    }

}
