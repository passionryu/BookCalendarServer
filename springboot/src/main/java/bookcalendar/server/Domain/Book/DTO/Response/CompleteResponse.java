package bookcalendar.server.Domain.Book.DTO.Response;

public record CompleteResponse(
        String bookName,
        String author,
        String reason
) {
}
