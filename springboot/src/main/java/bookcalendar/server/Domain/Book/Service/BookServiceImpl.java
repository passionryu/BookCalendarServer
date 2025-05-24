package bookcalendar.server.Domain.Book.Service;

import bookcalendar.server.Domain.Book.DTO.Request.BookRegisterRequest;
import bookcalendar.server.Domain.Book.DTO.Request.PeriodRequest;
import bookcalendar.server.Domain.Book.DTO.Request.SaveBookAutoRequest;
import bookcalendar.server.Domain.Book.DTO.Response.BookResponse;
import bookcalendar.server.Domain.Book.DTO.Response.CompleteResponse;
import bookcalendar.server.Domain.Book.DTO.Response.PeriodResponse;
import bookcalendar.server.Domain.Book.Entity.Book;
import bookcalendar.server.Domain.Book.Helper.BookHelper;
import bookcalendar.server.Domain.Book.Manager.BookManager;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Mypage.Entity.Cart;
import bookcalendar.server.global.Security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    @Cacheable(value = "bookInfo", key = "#customUserDetails.memberId")
    public BookResponse bookInfo(CustomUserDetails customUserDetails) {

        log.info("==> Cache Miss (도서 정보 반환): DB에서 도서 정보를 가져옵니다.");
        return bookManager.getCurrentReadingBookInfo(customUserDetails);
    }

    /* 독서할 도서 등록 메서드 */
    // 도서 정보 조회 캐시 삭제
    // 이번달의 도서 리스트 캐시 삭제
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "bookInfo", key = "#customUserDetails.memberId"),
            @CacheEvict(value = "mainPageResponse", key = "#customUserDetails.memberId")
    })

    public Book registerBook(BookRegisterRequest request, CustomUserDetails customUserDetails) {

        bookManager.checkReadingBookExist(customUserDetails.getMemberId()); // 이미 독서중인 도서가 있는지 검증

        bookManager.evictMonthlyBookListCache(customUserDetails.getMemberId()); // getPeriodList 캐시 삭제 메서드 호출

        return bookManager.registerBook(request, customUserDetails);
    }

    /* 독서 포기 메서드 */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "bookInfo", key = "#customUserDetails.memberId"),
            @CacheEvict(value = "mainPageResponse", key = "#customUserDetails.memberId")
    })

    public void giveUpReading(CustomUserDetails customUserDetails) {

        bookManager.giveUpReading(customUserDetails);
    }

    /* 독서 중인 도서 독서 완료 메서드 */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "bookInfo", key = "#customUserDetails.memberId"),
            @CacheEvict(value = "mainPageResponse", key = "#customUserDetails.memberId")
    })
    public List<CompleteResponse> completeReading(CustomUserDetails customUserDetails) {

        Member member = bookManager.getmember(customUserDetails.getMemberId());
        Book book = bookManager.getRedaingBook(customUserDetails.getMemberId());

        return bookManager.completeReading(member,book);
    }

    /* 추천 도서에서 저장 버튼을 눌러 장바구니에 책 저장 메서드 */
    @Override
    @Transactional
    public Cart saveBookToCartByAuto(CustomUserDetails customUserDetails, SaveBookAutoRequest saveBookAutoRequest) {

        return bookManager.saveAutoCart(customUserDetails, saveBookAutoRequest);
    }

    /* 등록된 도서 리스트를 메인페이지에 표시 하는 메서드 */
    @Override
    @Cacheable(value = "monthlyBookList", key = "#customUserDetails.memberId + '-' + #periodRequest.month()")
    public List<PeriodResponse> getPeriodList(CustomUserDetails customUserDetails, PeriodRequest periodRequest) {

        int month = periodRequest.month();
        int year = LocalDate.now().getYear(); // 요청에서 연도도 받으려면 구조 확장 가능

        LocalDate start = BookHelper.getStartOfMonth(year, month);
        LocalDate end = BookHelper.getEndOfMonth(start);

        Integer memberId = customUserDetails.getMemberId();

        log.info("==> Cache Miss (이번달 도서 리스트 정보 반환): DB에서 도서 리스트 정보를 가져옵니다.");
        return bookManager.getBooksPeriodInMonth(memberId, start, end);
    }

}
