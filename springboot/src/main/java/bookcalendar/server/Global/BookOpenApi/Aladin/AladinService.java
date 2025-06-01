package bookcalendar.server.global.BookOpenApi.Aladin;

import bookcalendar.server.Domain.Book.DTO.Response.CompleteResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AladinService {

    private final RestTemplate restTemplate;
    private final String aladinApiKey;
    private final String aladinSearchUrl;
    private final ObjectMapper objectMapper;
    private final ChatClient chatClient;

    public AladinService(RestTemplate restTemplate,
                         @Value("${aladinApiKey}") String aladinApiKey,
                         @Value("${aladinSearchUrl}") String aladinSearchUrl,
                         ObjectMapper objectMapper, ChatClient chatClient) {
        this.restTemplate = restTemplate;
        this.aladinApiKey = aladinApiKey;
        this.aladinSearchUrl = aladinSearchUrl;
        this.objectMapper = objectMapper;
        this.chatClient = chatClient;
    }

    /**
     * 주제를 토대로 도서 리스트 반환 (최상단부터 최대 count개)
     *
     * @param topic 주제 키워드
     * @param count 최대 반환할 도서 수
     * @return 도서 리스트 (Optional 포함)
     */
    public List<Optional<CompleteResponse>> searchTopBooksByTopic(String topic, int count) {
        List<Optional<CompleteResponse>> bookList = new ArrayList<>();

        try {
            String url = UriComponentsBuilder.fromHttpUrl(aladinSearchUrl)
                    .queryParam("ttbkey", aladinApiKey)
                    .queryParam("Query", topic)
                    .queryParam("QueryType", "Keyword")
                    .queryParam("Output", "JS")
                    .queryParam("Version", "20131101")
                    .build()
                    .toUriString();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode items = rootNode.path("item");

            if (items.isArray()) {

                  /* 구버전 코드 : [문제점 발견] - description이 반환되지 않는 경우 종종 발생 */
//                for (int i = 0; i < Math.min(count, items.size()); i++) {
//                    JsonNode book = items.get(i);
//
//                    Optional<CompleteResponse> completeResponse = Optional.of(
//                            CompleteResponse.builder()
//                                    .bookName(book.path("title").asText())
//                                    .author(book.path("author").asText())
//                                    .reason(book.path("description").asText()) // description은 itemDescription 또는 description
//                                    .url(book.path("link").asText())
//                                    .build()
//                    );

                for (int i = 0; i < Math.min(count, items.size()); i++) {
                    JsonNode book = items.get(i);

                    String title = book.path("title").asText();
                    String author = book.path("author").asText();
                    String description = book.path("description").asText();

                    // 방어 코드 1 : description이 null이거나 빈 문자열인 경우 GPT로 대체 설명 생성
                    if (description == null || description.isBlank()) {
                        description = generateTempDescriptionWithGpt(title, author);
                    }

                    Optional<CompleteResponse> completeResponse = Optional.of(
                            CompleteResponse.builder()
                                    .bookName(title)
                                    .author(author)
                                    .reason(description)
                                    .url(book.path("link").asText())
                                    .build()
                    );

                    bookList.add(completeResponse);
                }
            }
        } catch (Exception e) {
            log.warn("알라딘 다중 도서 검색 실패: topic={}, message={}", topic, e.getMessage());
        }

        return bookList;
    }

    /**
     * 설명이 없을 시, 제목&저자 이름으로 description을 추가하는 메서드
     *
     * @param title 도서의 제목
     * @param author 도서의 저자
     * @return 해당 도서의 설명
     */
    private String generateTempDescriptionWithGpt(String title, String author) {

        log.info("title={}, author={} - 이 책에 대해 설명을 추출하지 못하여 Gpt가 임시 생성합니다.", title, author);

        String prompt = String.format("""
        책 제목: %s
        저자: %s

        위의 책 정보를 바탕으로, 책의 주제나 내용을 추정해서 40~60자 내외로 짧은 설명 문장을 만들어줘.
        명확한 정보가 없어도 적당한 추정 설명을 생성해.
    """, title, author);

        try {
            // chatClient는 사용자의 기존 GPT 통신 유틸이라 가정
            String response = chatClient.call(prompt);
            return response.trim();
        } catch (Exception e) {
            log.warn("GPT 설명 생성 실패: title={}, author={}, message={}", title, author, e.getMessage());
            return "이 책에 대한 자세한 설명은 준비 중입니다.";
        }
    }


}
