package bookcalendar.server.Domain.Book.DTO.Response;

import java.time.LocalDate;

public record BookResponse(
        String bookName,
        String author,
        Integer totalPage,
        String genre,
        LocalDate startDate,
        LocalDate finishDate
) {
}
