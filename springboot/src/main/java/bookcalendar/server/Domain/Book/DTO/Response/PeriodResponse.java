package bookcalendar.server.Domain.Book.DTO.Response;

import java.time.LocalDate;

public record PeriodResponse(
        Integer BookId,
        String BookName,
        LocalDate startDate,
        LocalDate finishDate,
        String color
) {
}
