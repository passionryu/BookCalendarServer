package bookcalendar.server.Domain.Mypage.DTO.Request;

public record ManualCartRequest(
    String bookName,
    String author,
    String url
) {
}
