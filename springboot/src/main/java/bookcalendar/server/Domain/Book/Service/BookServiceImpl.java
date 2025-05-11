package bookcalendar.server.Domain.Book.Service;

import bookcalendar.server.Domain.Book.DTO.Request.BookRegisterRequest;
import bookcalendar.server.Domain.Book.DTO.Request.PeriodRequest;
import bookcalendar.server.Domain.Book.DTO.Request.SaveBookAutoRequest;
import bookcalendar.server.Domain.Book.DTO.Response.BookResponse;
import bookcalendar.server.Domain.Book.DTO.Response.CompleteResponse;
import bookcalendar.server.Domain.Book.DTO.Response.PeriodResponse;
import bookcalendar.server.Domain.Book.Entity.Book;
import bookcalendar.server.Domain.Book.Manager.BookManager;
import bookcalendar.server.Domain.Mypage.Entity.Cart;
import bookcalendar.server.global.Security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookManager bookManager;

    /* 현재 독서중인 도서 존재 확인 메서드 */
    @Override
    public boolean bookExist(CustomUserDetails customUserDetails) {
        return bookManager.bookExist(customUserDetails);
    }

    /* 현재 독서 중인 도서 정보 상세 반환 메서드  */
    @Override
    public BookResponse bookInfo(CustomUserDetails customUserDetails) {
        return bookManager.getCurrentReadingBookInfo(customUserDetails);
    }

    /* 독서할 도서 등록 메서드 */
    @Override
    @Transactional
    public Book registerBook(BookRegisterRequest request, CustomUserDetails customUserDetails) {

        bookManager.checkReadingBookExist(customUserDetails.getMemberId()); // 이미 독서중인 도서가 있는지 검증
        return bookManager.registerBook(request, customUserDetails);
    }

    /* 독서 포기 메서드 */
    @Override
    @Transactional
    public void giveUpReading(CustomUserDetails userDetails) {
        bookManager.giveUpReading(userDetails);
    }

    /* 독서 중인 도서 독서 완료 메서드 */
    @Override
    @Transactional
    public List<CompleteResponse> completeReading(CustomUserDetails userDetails) {
        return bookManager.completeReading(userDetails);
    }

    /* 추천 도서에서 저장 버튼을 눌러 장바구니에 책 저장 메서드 */
    @Override
    @Transactional
    public Cart saveBookToCartByAuto(CustomUserDetails userDetails, SaveBookAutoRequest request) {
        return bookManager.saveAutoCart(userDetails, request);
    }

    /* 등록된 도서를 캘린더에 선으로 표시 하는 메서드 */
    @Override
    public List<PeriodResponse> getPeriodList(CustomUserDetails userDetails, PeriodRequest request) {
        return bookManager.getBooksPeriodInMonth(userDetails, request);
    }


}
