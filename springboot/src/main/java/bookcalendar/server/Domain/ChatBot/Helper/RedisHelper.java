package bookcalendar.server.Domain.ChatBot.Helper;

import bookcalendar.server.Domain.Book.DTO.Response.CompleteResponse;
import bookcalendar.server.Domain.ChatBot.Exception.ChatBotException;
import bookcalendar.server.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.util.logging.Slf4j;

import java.util.List;

@Slf4j
public class RedisHelper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 메시지 프롬프팅 메서드
     *
     * @param message 사용자의 이번 메시지
     * @param previousMessage 사용자의 기존 메시지
     * @return 프롬프트 메시지
     */
    public static String makePromptMessage(String message,String previousMessage){

        return "당신의 역할은 AI 도서 추천 전문가이다. "
                + "당신이 이전에 사용자와 나눈 내용은 다음과 같다"
                + previousMessage
                + "이를 참고하여 사용자의 다음 메시지를 보고 적절히 답변하라."
                + message;
    }

    /**
     * AI 응답 메시지에서 'counselor:' 접두어를 제거
     *
     * @param aiResponse 원본 AI 응답 문자열
     * @return 정제된 문자열
     */
    public static String textCleaner(String aiResponse) {
        if (aiResponse == null) return "";
        return aiResponse.replaceAll("(?i)^counselor:\\s*", "");
    }

    public static String getPromptMessage(String everyMessages){

        return String.format(
                """
                너는 이 도서 추천 서비스의 유능한 AI 도서 추천 사서이다.
                다음은 너가 사용자와의 챗봇 서비스에서 나눈 대화 내용이다
                다음은 내용들을 참고하여 사용자에게 도서 5권을 추천해w:
    
                - 너(AI 도서 추천 사서 와 사용자와의 지난 모든 대화): "%s"
    
                아래 JSON 형식으로 꼭 반환해줘:
    
                [
                  {
                    "bookName": "책 제목",
                    "author": "저자 이름",
                    "reason": "이 도서를 추천하는 이유2~3줄"
                  },
                  ...
                ]
                """,
                everyMessages
        );

    }

    /**
     * 리스트 파서 메서드
     *
     * @param aiResponse
     * @return
     */
    public static List<CompleteResponse> parseJsonArray(String aiResponse){

        List<CompleteResponse> recommendations;

        try {
            recommendations = objectMapper.readValue(
                    aiResponse,
                    new TypeReference<>() {}
            );
        } catch (JsonProcessingException e) {
            throw new ChatBotException(ErrorCode.FAILED_TO_PARSE);
        }
        return recommendations;
    }

}
