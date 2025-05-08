package bookcalendar.server.Domain.Review.Helper;

public class ReviewHelper {

    /* 1번 질문지 생성 메서드 */
    public static String makeQuestion1(String emotion) {

        return "오늘의 Daily 독후감에서는" + emotion + "의 감정을 담고 있네요. " +
                "이 감정을 글로 표현하면서 오늘은 어떤 장면이 " + emotion + "의 감정을 더욱 느껴지게 했나요?";
    }
}
