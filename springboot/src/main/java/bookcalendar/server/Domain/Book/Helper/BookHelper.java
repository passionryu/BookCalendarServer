package bookcalendar.server.Domain.Book.Helper;

import bookcalendar.server.Domain.Book.DTO.Request.SaveBookAutoRequest;
import bookcalendar.server.Domain.Book.DTO.Response.CompleteResponse;
import bookcalendar.server.Domain.Book.Entity.Book;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Mypage.Entity.Cart;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    public static String buildPrompt(Book book, List<String> emotions, Member member, int age) {
        return String.format("""
    다음 정보를 참고해서 사용자에게 도서 5권을 **정확히** 추천해줘.
    반드시 5권 모두 추천해야 하며, 부족하거나 초과하지 말고 정확히 5권만 응답해야 해:

    - 읽은 책: "%s"
    - 장르: %s
    - 감정 목록: %s
    - 사용자 나이: %d살
    - 선호 장르: %s
    - 직업: %s

    아래 형식을 참고해서 **반드시 JSON 배열에 5개의 도서를 포함해 반환**해줘:

    [
      {
        "bookName": "책 제목",
        "author": "저자 이름",
        "reason": "이 도서를 추천하는 이유 (2~3줄)"
      },
      ...
      (총 5개 항목)
    ]
    """,
                book.getBookName(),
                book.getGenre(),
                emotions,
                age,
                member.getGenre(),
                member.getJob()
        );
    }

    /* JSON 응답 파싱 함수 */
    public static List<CompleteResponse> parseRecommendations(String aiResponse) {

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
            throw new RuntimeException("AI 응답 파싱 실패: " + e.getMessage(), e);
        }
        return recommendations;
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
