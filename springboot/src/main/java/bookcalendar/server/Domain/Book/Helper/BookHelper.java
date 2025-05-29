package bookcalendar.server.Domain.Book.Helper;

import bookcalendar.server.Domain.Book.DTO.Request.SaveBookAutoRequest;
import bookcalendar.server.Domain.Book.DTO.Response.CompleteResponse;
import bookcalendar.server.Domain.Book.Entity.Book;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Mypage.Entity.Cart;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BookHelper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // ======================= 도서 등록 영역 =========================

    /* 각 도서 별 고유 색감 랜덤 부여 */
    public static String getRandomColor() {
        Random random = new Random();

        // 밝은 색감을 위해 각 RGB 채널을 128~255 범위로 설정
        int r = 128 + random.nextInt(128); // 128 ~ 255
        int g = 128 + random.nextInt(128);
        int b = 128 + random.nextInt(128);

        return String.format("#%02X%02X%02X", r, g, b); // 대문자 HEX로 출력
    }

    // ======================= 독서 완료 후 추천 도서 반환 영역 =========================

    /* 별도 프롬프트 구성 함수 */
    public static String buildPrompt(Book book, List<String> contentsList, Member member, int age) {
        return String.format("""
            당신은 사용자의 독서 데이터를 기반으로 주요 주제를 정밀하게 추출하는 AI입니다.
        
            아래는 한 사용자의 독서 정보입니다.
            이 사용자의 독후감과 정보를 바탕으로 메인 주제를 **2개만**, **단어 형식**으로 추출하세요.
            
            추출된 주제는 알라딘 도서 검색 API를 통해 책을 추천하는 데 사용됩니다.
            
            반드시 주제 2개를 각각 **단어 하나의 형식**으로 추출하세요.
            다른 설명은 하지 마세요.
        
            [입력 정보]
            - 읽은 책: "%s"
            - 장르: %s
            - 사용자 나이: %d살
            - 선호 장르: %s
            - 직업: %s
            - 유저가 이 책에 대해 작성한 독후감 리스트: %s
        
             - 응답은 반드시 아래 JSON 형식으로 반환하라:
           
                {
                  "topics": ["인간관계"], ["성장"]
                }
            """
                ,
                book.getBookName(),
                book.getGenre(),
                age,
                member.getGenre(),
                member.getJob(),
                contentsList
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

    // ======================= 장바구니 객체 생성 영역 =========================

    public static Cart createCartFromRequest(SaveBookAutoRequest request, Member member) {
        return Cart.builder()
                .bookName(request.bookName())
                .author(request.author())
                .link(request.url())
                .date(LocalDateTime.now())
                .member(member)
                .build();
    }

    // ======================= 독서기간 캘린더에 선으로 표시하는 영역 =========================

    public static LocalDate getStartOfMonth(int year, int month) {
        return LocalDate.of(year, month, 1);
    }

    public static LocalDate getEndOfMonth(LocalDate startOfMonth) {
        return startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
    }
}
