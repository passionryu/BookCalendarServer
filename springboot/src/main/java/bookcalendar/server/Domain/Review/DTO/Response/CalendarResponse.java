package bookcalendar.server.Domain.Review.DTO.Response;

import java.time.LocalDate;

public record CalendarResponse(
        Integer reviewId,
        Integer progress,
        LocalDate date,
        String color
) {
}
