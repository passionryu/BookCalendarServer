package bookcalendar.server.global.BookOpenApi.NationalCentralLibrary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class NationalCentralLibraryService {

    private final RestTemplate restTemplate;
    private final String nclApiKey;
    private final String nclSearchUrl;
    private final ObjectMapper objectMapper;

    public NationalCentralLibraryService(RestTemplate restTemplate,
                                         @Value("${nclApiKey:3c113b7a081fe5cf98ec555506615ae3e9335704edfa0d4c047ee592a1a4eb52}") String nclApiKey,
                                         @Value("${nclSearchUrl}") String nclSearchUrl,
                                         ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.nclApiKey = nclApiKey;
        this.nclSearchUrl = nclSearchUrl;
        this.objectMapper = objectMapper;
    }

    public NationalCentralLibraryResponse searchBook(String bookTitle, String author) throws Exception {
        log.info("National Central Library URL 반환");
        // 국립중앙도서관 검색 API URL 구성
        String url = UriComponentsBuilder.fromHttpUrl(nclSearchUrl)
                .queryParam("key", nclApiKey)
                .queryParam("query", bookTitle + " " + author) // 제목과 저자 결합
                .queryParam("resultStyle", "json")
                .build()
                .toUriString();

        // API 호출
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // JSON 파싱
        JsonNode rootNode = objectMapper.readTree(response.getBody());
        JsonNode itemsNode = rootNode.path("items");

        if (itemsNode.isArray() && itemsNode.size() > 0) {
            JsonNode firstItem = itemsNode.get(0);
            String title = firstItem.path("title").asText().replaceAll("<[^>]+>", "");
            String link = firstItem.path("link").asText();
            return new NationalCentralLibraryResponse(title, link);
        } else {
            throw new Exception("No book found for title: " + bookTitle + " and author: " + author);
        }
    }
}