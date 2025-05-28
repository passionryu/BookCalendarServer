package bookcalendar.server.Domain.ChatBot.Helper;

import bookcalendar.server.Domain.Book.DTO.Response.CompleteResponse;
import bookcalendar.server.Domain.ChatBot.Exception.ChatBotException;
import bookcalendar.server.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.util.logging.Slf4j;

import java.util.ArrayList;
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
     * Gpt에게 2가지의 대표 주제를 추출하도록 요청하는 프롬프트 메시지 생성기
     *
     * @param everyMessages 유저와 챗봇의 모든 대화
     * @return 프롬프트 메시지
     */
    public static String buildPrompt_getTopic(String everyMessages) {
        return String.format(
                """
                너는 유능한 AI 분석가이다. 아래는 사용자와 챗봇이 나눈 대화 내용이다:
                
                - 사용자와의 대화 내용: "%s"
                
                위 대화를 바탕으로, 사용자의 관심사 및 주요 대화 흐름을 분석하여 
                **핵심 주제 2가지**를 추출하라.
                
                반드시 다음 조건을 지켜라:
                - 주제는 **단어 단위(명사 형태)**로 추출해야 한다 (예: "리더십", "자기계발", "프로그래밍").
                - 반드시 **정확히 2개의 주제 단어**만 추출하라. 그 이상도 이하도 안 된다.
                - 이 2개는 전체 대화를 가장 잘 대표하는 핵심 주제어여야 한다.
                - **주제를 상위 카테고리로 추상화하지 말고, 가능한 한 구체적인 관심사나 개념을 선택하라.**
                - 응답은 반드시 아래 JSON 형식으로 반환하라:
                
                {
                  "topics": ["주제1", "주제2"]
                }
                
                대화가 모호하더라도, 사용자의 흥미를 기반으로 **적절한 2가지 단어 주제**를 반드시 생성하라.
                """,
                everyMessages
        );
    }


    /**
     * GPT가 응답한 JSON 문자열에서 "topics" 배열을 파싱하여 List<String>으로 반환
     *
     * @param gptResponse GPT가 반환한 JSON 문자열
     * @return 주제 리스트 (정상적으로 파싱 실패 시 빈 리스트 반환)
     */
    public static List<String> parseTopicList(String gptResponse) {
        List<String> topicList = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(gptResponse);

            JsonNode topicsArray = rootNode.get("topics");
            if (topicsArray != null && topicsArray.isArray()) {
                for (JsonNode topic : topicsArray) {
                    topicList.add(topic.asText());
                }
            }
        } catch (Exception e) {
            // 파싱 실패 로그 출력 또는 처리
            System.err.println("GPT topic 파싱 실패: " + gptResponse);
            e.printStackTrace();
        }

        return topicList;
    }

}
