package bookcalendar.server.Domain.Book.Service;

import bookcalendar.server.Domain.Book.DTO.Request.BookRegisterRequest;
import bookcalendar.server.Domain.Book.DTO.Request.PeriodRequest;
import bookcalendar.server.Domain.Book.DTO.Request.SaveBookAutoRequest;
import bookcalendar.server.Domain.Book.DTO.Response.BookResponse;
import bookcalendar.server.Domain.Book.DTO.Response.CompleteResponse;
import bookcalendar.server.Domain.Book.DTO.Response.PeriodResponse;
import bookcalendar.server.Domain.Book.Entity.Book;
import bookcalendar.server.Domain.Mypage.Entity.Cart;
import bookcalendar.server.global.Security.CustomUserDetails;

import java.util.List;

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

    /**
     * 독서 포기 인터페이스
     *
     * @param customUserDetails
     */
    void giveUpReading(CustomUserDetails customUserDetails);

    /**
     * 독서 완료 인터페이스
     *
     * @param customUserDetails
     * @return
     */
    List<CompleteResponse>  completeReading(CustomUserDetails customUserDetails);

    /**
     *  등록된 도서리스트를 메인페이지에 표시 하는 메서드
     *
     * @param customUserDetails
     * @param periodRequest
     * @return
     */
    List<PeriodResponse> getPeriodList(CustomUserDetails customUserDetails, PeriodRequest periodRequest);

    /**
     * 장바구니에 도서 자동 저장 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param saveBookAutoRequest 저장하고자 하는 도서의 정보 DTO
     * @return 장바구니 객체
     */
    Cart saveBookToCartByAuto(CustomUserDetails customUserDetails, SaveBookAutoRequest saveBookAutoRequest);

}
