package bookcalendar.server.Domain.ChatBot.Helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class ChatBotHelper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /* 도서 주제 블랙리스트 */
    private static final Set<String> INVALID_TOPICS = Set.of("책", "도서", "서적", "추천", "중고", "알라딘",
            "포장팩", "가방", "쇼핑", "상품", "판매");

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
     * Gpt에게 1가지의 대표 주제를 추출하도록 요청하는 프롬프트 메시지 생성기
     *
     * @param everyMessages 유저와 챗봇의 모든 대화
     * @return 프롬프트 메시지
     */
    public static String buildPrompt_getTopic(String everyMessages) {

        /* 블랙리스트 */
        String blacklist = INVALID_TOPICS.stream()
                .map(word -> "\"" + word + "\"") // 각각의 항목에 "붙이기
                .collect(Collectors.joining(", ", "[", "]")); // JSON 배열 형식으로 변환

        return String.format(
                """
                너는 유능한 AI 분석가이다. 아래는 사용자와 챗봇이 나눈 대화 내용이다:
                
                - 사용자와의 대화 내용: "%s"
                
                위 대화를 바탕으로, 사용자의 관심사 및 주요 대화 흐름을 분석하여 
                **핵심 주제 1가지**를 추출하라.
                
                반드시 다음 조건을 지켜라:
                - 주제는 **단어 단위(명사 형태)**로 추출해야 한다 (예: "리더십", "자기계발", "프로그래밍").
                - 반드시 **정확히 1개의 주제 단어**만 추출하라. 그 이상도 이하도 안 된다.
                - 이 1개는 전체 대화를 가장 잘 대표하는 핵심 주제어여야 한다.
                - **주제를 상위 카테고리로 추상화하지 말고, 가능한 한 구체적인 관심사나 개념을 선택하라.**
                 - 다음 주제 단어들은 블랙리스트이다. 절대 출력해서는 안 된다: %s
                - 응답은 반드시 아래 JSON 형식으로 반환하라:
                
                {
                  "topics": ["주제1"]
                }
                
                대화가 모호하더라도, 사용자의 흥미를 기반으로 **적절한 1가지 단어 주제**를 반드시 생성하라.
                """,
                everyMessages, blacklist
        );
    }

    /**
     * GPT가 응답한 JSON 문자열에서 "topics" 배열의 첫 번째 요소를 파싱하여 반환
     *
     * @param gptResponse GPT가 반환한 JSON 문자열
     * @return 첫 번째 주제 문자열 (파싱 실패 시 null 반환)
     */
    public static String parseTopic(String gptResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(gptResponse);

            JsonNode topicsArray = rootNode.get("topics");
            if (topicsArray != null && topicsArray.isArray() && !topicsArray.isEmpty()) {
                return topicsArray.get(0).asText();  // 첫 번째 주제만 추출
            }
        } catch (Exception e) {
            log.info("GPT topic 파싱 실패: {}", gptResponse);
            e.printStackTrace();
        }

        return null;  // 파싱 실패 시 null
    }


}
