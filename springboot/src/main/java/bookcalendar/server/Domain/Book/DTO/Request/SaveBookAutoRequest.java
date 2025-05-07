package bookcalendar.server.Domain.Book.DTO.Request;

public record SaveBookAutoRequest(
        String bookName,
        String author,
        String url
) {
}
