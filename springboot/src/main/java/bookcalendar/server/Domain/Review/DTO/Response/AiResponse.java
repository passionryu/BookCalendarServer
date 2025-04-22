package bookcalendar.server.Domain.Review.DTO.Response;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record AiResponse(
        Integer totalPages,
        Integer currentPages,
        Integer progress,
        LocalDate finishDate,
        Integer remainDate,
        String averageMessage,
        String aiMessage
) {}
