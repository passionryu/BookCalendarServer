package bookcalendar.server.global.Aladin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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

    public AladinResponse searchBook(String bookTitle, String author) throws Exception {
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
