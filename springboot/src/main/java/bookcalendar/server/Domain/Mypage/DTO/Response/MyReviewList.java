package bookcalendar.server.Domain.Mypage.DTO.Response;

import java.time.LocalDate;

public record MyReviewList(
        Integer reviewId,
        String bookName,
        LocalDate date
) {
}
