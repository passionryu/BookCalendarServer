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

    /**
     * 프롬프트 메시지 커스터마이징 메서드
     *
     * @param everyMessages 모든 챗봇 대화 메시지
     * @return 프롬프트 메시지
     */
    public static String getPromptMessage(String everyMessages) {
        return String.format(
                """
                너는 이 도서 추천 서비스의 유능한 AI 도서 추천 사서이다.
                다음은 사용자와의 챗봇 서비스에서 나눈 대화 내용이다:
                
                - 사용자와의 지난 모든 대화: "%s"
                
                이 대화 내용을 바탕으로 사용자에게 **정확히 5권의 도서**를 추천해야 한다. 
                각 도서는 사용자의 관심사와 대화 맥락에 맞춰 선정하고, 아래 조건을 반드시 준수하라:
                - 추천 도서는 정확히 5권이어야 하며, 그 이상도 이하도 안 된다.
                - 각 도서의 추천 사유는 2~3문장으로 간결하고 명확하게 작성하라.
                - 응답은 반드시 아래 JSON 형식으로 반환하라:
                
                [
                  {
                    "bookName": "책 제목",
                    "author": "저자 이름",
                    "reason": "이 도서를 추천하는 이유 (2~3문장)"
                  },
                  ...
                ]
                
                대화 내용이 모호하거나 정보가 부족하더라도, 사용자의 관심사를 추론하여 5권을 반드시 추천하라.
                """,
                everyMessages
        );
    }

    // 이전 프롬프트 메시지 메서드 . 2025.05/19
//    public static String getPromptMessage(String everyMessages){
//
//        return String.format(
//                """
//                너는 이 도서 추천 서비스의 유능한 AI 도서 추천 사서이다.
//                다음은 너가 사용자와의 챗봇 서비스에서 나눈 대화 내용이다
//                다음은 내용들을 참고하여 사용자에게 도서 5권을 추천해줘:
//
//                - 너(AI 도서 추천 사서 와 사용자와의 지난 모든 대화): "%s"
//
//                아래 JSON 형식으로 꼭 반환해줘:
//
//                [
//                  {
//                    "bookName": "책 제목",
//                    "author": "저자 이름",
//                    "reason": "이 도서를 추천하는 이유2~3줄"
//                  },
//                  ...
//                ]
//                """,
//                everyMessages
//        );
//
//    }

    /**
     * 리스트 파서 메서드
     *
     * @param aiResponse
     * @return
     */
    public static List<CompleteResponse> parseJsonArray(String aiResponse){

        List<CompleteResponse> recommendations;

        try {
            // JSON 배열을 파싱하되, url 필드가 없어도 매핑 가능하도록
            List<CompleteResponse> tempList = objectMapper.readValue(
                    aiResponse,
                    new TypeReference<List<CompleteResponse>>() {}
            );
            // url 필드를 빈 문자열로 초기화
            recommendations = tempList.stream()
                    .map(resp -> new CompleteResponse(resp.getBookName(), resp.getAuthor(), resp.getReason(), resp.getUrl()))
                    .toList();
        } catch (JsonProcessingException e) {
            throw new ChatBotException(ErrorCode.FAILED_TO_PARSE);
        }
        return recommendations;
    }

}
