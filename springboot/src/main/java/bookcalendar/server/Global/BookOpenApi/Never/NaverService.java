package bookcalendar.server.global.BookOpenApi.Never;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class NaverService {

    private final RestTemplate restTemplate;
    private final String naverClientId;
    private final String naverClientSecret;
    private final String naverSearchUrl;
    private final ObjectMapper objectMapper;

    public NaverService(RestTemplate restTemplate,
                        @Value("${naverClientId}") String naverClientId,
                        @Value("${naverClientSecret}") String naverClientSecret,
                        @Value("${naverSearchUrl}") String naverSearchUrl,
                        ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.naverClientId = naverClientId;
        this.naverClientSecret = naverClientSecret;
        this.naverSearchUrl = naverSearchUrl;
        this.objectMapper = objectMapper;
    }

    public NaverResponse searchBook(String bookTitle, String author) throws Exception {
        log.info("Naver URl 반환");
        // 네이버 검색 API URL 구성
        String url = UriComponentsBuilder.fromHttpUrl(naverSearchUrl)
                .queryParam("query", bookTitle + " " + author) // 제목과 저자 결합
                .queryParam("display", 1) // 첫 번째 결과만 반환
                .build()
                .toUriString();

        // 헤더 설정 (클라이언트 ID와 시크릿 추가)
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", naverClientId);
        headers.set("X-Naver-Client-Secret", naverClientSecret);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // API 호출
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        // JSON 파싱
        JsonNode rootNode = objectMapper.readTree(response.getBody());
        JsonNode itemsNode = rootNode.path("items");

        if (itemsNode.isArray() && itemsNode.size() > 0) {
            JsonNode firstItem = itemsNode.get(0);
            String title = firstItem.path("title").asText().replaceAll("<[^>]+>", ""); // HTML 태그 제거
            String link = firstItem.path("link").asText();
            return new NaverResponse(title, link);
        } else {
            throw new Exception("No book found for title: " + bookTitle + " and author: " + author);
        }
    }
}
