package bookcalendar.server.Domain.Review.Helper;

import bookcalendar.server.Domain.Book.Entity.Book;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Slf4j
public class ReviewHelper {

    /* 1번 질문지 생성 메서드 */
    public static String makeQuestion1(String emotion) {

        return "오늘의 Daily 독후감에서는 " + emotion + "의 감정을 담고 있네요. " +
                "이 감정을 글로 표현하면서 오늘은 어떤 장면이 " + emotion + "의 감정을 더욱 느껴지게 했나요?";
    }

    // ======================= 메인 페이지 독후감 진행률 & 남은 독서일 조회 메서드 영역 =========================

    /* 독서 예정일까지 남은 날짜 계산 Helper 메서드 */
    public static String calculateRemainDate(Book book){

        LocalDate today = LocalDate.now();
        LocalDate finishDate = book.getFinishDate();
        long diff = ChronoUnit.DAYS.between(today, finishDate);

        // remainDate를 부호 포함 문자열로 반환하고 싶다면:
        String remainDate;
        if (diff > 0) {
            remainDate = "-" + diff; // 마감일까지 남은 날 (예: -2)
        } else if (diff < 0) {
            remainDate = "+" + Math.abs(diff); // 마감일이 지난 날 (예: +3)
        } else {
            remainDate = "0"; // 마감일이 오늘
        }

        log.info("LocalDate.now() = {}", today);
        log.info("book.getFinishDate() = {}", finishDate);
        log.info("계산된 remainDate = {}", remainDate);

        return remainDate;
    }

}
