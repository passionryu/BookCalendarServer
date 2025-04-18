package bookcalendar.server.Domain.Book.Service;

import bookcalendar.server.Domain.Book.DTO.Request.BookRegisterRequest;
import bookcalendar.server.Domain.Book.DTO.Response.BookResponse;
import bookcalendar.server.Domain.Book.Entity.Book;
import bookcalendar.server.global.Security.CustomUserDetails;

public interface BookService {

    /**
     * 독서중인 도서 존재 여부 확인 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return True False
     */
    boolean bookExist(CustomUserDetails customUserDetails);

    /**
     * 도서 정보 반환 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 도서 정보
     */
    BookResponse bookInfo(CustomUserDetails customUserDetails);

    /**
     * 도서 등록 인터페이스
     *
     * @param bookRegisterRequest 도서 등록 데이터
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 도서 객체
     */
    Book registerBook(BookRegisterRequest bookRegisterRequest,CustomUserDetails customUserDetails);

}
