package bookcalendar.server.Domain.ChatBot.DTO.Request;

public record SaveBookRequest(
        String bookName,
        String author,
        String url
) {
}
