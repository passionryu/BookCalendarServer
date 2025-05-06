package bookcalendar.server.Domain.Mypage.DTO.Response;

import java.time.LocalDateTime;

public record MyScrapListResponse(
   Integer scrapId,
   String title,
   String author,
   LocalDateTime dateTime
) {
}
