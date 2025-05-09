package bookcalendar.server.Domain.Book.DTO.Request;

import bookcalendar.server.Domain.Book.Entity.Book;
import bookcalendar.server.Domain.Member.Entity.Member;

import java.time.LocalDate;

public record BookRegisterRequest(
        String bookName,
        String author,
        Integer totalPage,
        String genre,
        LocalDate startDate,
        LocalDate finishDate
) {
    public Book toEntity(Integer memberId) {
        return Book.builder()
                .bookName(bookName)
                .author(author)
                .totalPage(totalPage)
                .genre(genre)
                .startDate(startDate)
                .finishDate(finishDate)
                .status(Book.Status.독서중) // 기본값 지정
                .memberId(memberId)
                .registerDate(LocalDate.now())
                .build();
    }
}
