package bookcalendar.server.Domain.ChatBot.DTO.Request;

public record SaveBookAutoRequest(
        String bookName,
        String author,
        String url
) {
}
