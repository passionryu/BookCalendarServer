package bookcalendar.server.Domain.Review.DTO.Response;

public record ProgressResponse(
        Integer progress,
        Integer previousPages
) {
}
