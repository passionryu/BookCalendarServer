package bookcalendar.server.Domain.Review.DTO.Response;

import org.hibernate.id.IncrementGenerator;

public record MainPageResponse(
        Integer progress,
        Integer remainDate
) {
    public static MainPageResponse empty() {
        return new MainPageResponse(0, 0);
    }
}
