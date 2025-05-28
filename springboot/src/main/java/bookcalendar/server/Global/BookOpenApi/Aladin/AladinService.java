package bookcalendar.server.global.BookOpenApi.Aladin;

import bookcalendar.server.Domain.Book.DTO.Response.CompleteResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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

    public AladinService(RestTemplate restTemplate,
                             @Value("${aladinApiKey}") String aladinApiKey,
                             @Value("${aladinSearchUrl}") String aladinSearchUrl,
                             ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.aladinApiKey = aladinApiKey;
        this.aladinSearchUrl = aladinSearchUrl;
        this.objectMapper = objectMapper;
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
                for (int i = 0; i < Math.min(count, items.size()); i++) {
                    JsonNode book = items.get(i);

                    Optional<CompleteResponse> completeResponse = Optional.of(
                            CompleteResponse.builder()
                                    .bookName(book.path("title").asText())
                                    .author(book.path("author").asText())
                                    .reason(book.path("description").asText()) // description은 itemDescription 또는 description
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
     * 주제를 토대로 도서 리스트 반환 (각 주제별로 1권씩)
     *
     * @param topic 주제
     * @return 도서 리스트
     */
    public Optional<CompleteResponse> searchBookByTopic(String topic) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(aladinSearchUrl)
                    .queryParam("ttbkey", aladinApiKey)
                    .queryParam("Query", topic) // 주제 기반 검색
                    .queryParam("QueryType", "Keyword")
                    .queryParam("Output", "JS")
                    .queryParam("Version", "20131101")
                    .build()
                    .toUriString();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode items = rootNode.path("item");

            if (items.isArray() && items.size() > 0) {
                JsonNode book = items.get(0); // 가장 상단 책
                return Optional.of(
                        CompleteResponse.builder()
                                .bookName(book.path("title").asText())
                                .author(book.path("author").asText())
                                .reason(book.path("description").asText()) // itemDescription or description
                                .url(book.path("link").asText())
                                .build()
                );
            }
        } catch (Exception e) {
            log.warn("알라딘 도서 검색 실패: topic={}, message={}", topic, e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * 제목, 저자로 Url 반환 메서드
     *
     * @param bookTitle 도서 제목
     * @param author 저자
     * @return Url
     * @throws Exception
     */
    public AladinResponse searchBook(String bookTitle, String author) throws Exception {
        log.info("Aladin URL 반환");
        // 알라딘 검색 API URL 구성
        String url = UriComponentsBuilder.fromHttpUrl(aladinSearchUrl)
                .queryParam("ttbkey", aladinApiKey)
                .queryParam("Query", bookTitle + " " + author) // 제목과 저자 결합
                .queryParam("QueryType", "Keyword") // QueryType을 Keyword로 변경
                .queryParam("Output", "JS")
                .queryParam("Version", "20131101")
                .build()
                .toUriString();

        // API 호출
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // JSON 파싱
        JsonNode rootNode = objectMapper.readTree(response.getBody());
        JsonNode itemNode = rootNode.path("item");

        if (itemNode.isArray() && itemNode.size() > 0) {
            JsonNode firstItem = itemNode.get(0);
            String title = firstItem.path("title").asText();
            String link = firstItem.path("link").asText();
            return new AladinResponse(title, link);
        } else {
            throw new Exception("No book found for title: " + bookTitle + " and author: " + author);
        }
    }
}
