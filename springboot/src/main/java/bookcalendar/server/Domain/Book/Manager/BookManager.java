package bookcalendar.server.Domain.Book.Manager;

import bookcalendar.server.Domain.Book.DTO.Request.BookRegisterRequest;
import bookcalendar.server.Domain.Book.DTO.Request.PeriodRequest;
import bookcalendar.server.Domain.Book.DTO.Request.SaveBookAutoRequest;
import bookcalendar.server.Domain.Book.DTO.Response.BookResponse;
import bookcalendar.server.Domain.Book.DTO.Response.CompleteResponse;
import bookcalendar.server.Domain.Book.DTO.Response.PeriodResponse;
import bookcalendar.server.Domain.Book.Entity.Book;
import bookcalendar.server.Domain.Book.Exception.BookException;
import bookcalendar.server.Domain.Book.Helper.BookHelper;
import bookcalendar.server.Domain.Book.Repository.BookRepository;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Exception.MemberException;
import bookcalendar.server.Domain.Member.Repository.MemberRepository;
import bookcalendar.server.Domain.Mypage.Entity.Cart;
import bookcalendar.server.Domain.Mypage.Repository.CartRepository;
import bookcalendar.server.Domain.Review.Entity.Review;
import bookcalendar.server.Domain.Review.Repository.ReviewRepository;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookManager {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final ReviewRepository reviewRepository;
    private final ChatClient chatClient;

    // ======================= 독서중인 도서 정보 반환 영역 =========================

    /* 현재 독서중인 도서 존재 확인 메서드 */
    public boolean bookExist(CustomUserDetails customUserDetails) {
        return bookRepository.existsByMemberIdAndStatus(
                customUserDetails.getMemberId(),
                Book.Status.독서중);
    }

    /* 현재 독서 중인 도서 정보 상세 반환 메서드  */
    public BookResponse getCurrentReadingBookInfo(CustomUserDetails customUserDetails) {
        Book book = bookRepository.findByMemberIdAndStatus(customUserDetails.getMemberId(), Book.Status.독서중)
                .orElseThrow(() -> new MemberException(ErrorCode.BOOK_NOT_FOUND));

        return new BookResponse(
                book.getBookName(),
                book.getAuthor(),
                book.getTotalPage(),
                book.getGenre(),
                book.getStartDate(),
                book.getFinishDate());
    }
    // ======================= 도서 등록 로직 =========================

    /* 이미 독서 중인 도서가 있는지 검증하는 메서드 */
    public void checkReadingBookExist(Integer memberId) {
        if (bookRepository.existsByMemberIdAndStatus(memberId, Book.Status.독서중))
            throw new BookException(ErrorCode.READING_BOOK_ALREADY_EXIST);
    }

    /* 독서할 도서 등록 메서드 */
    public Book registerBook(BookRegisterRequest bookRegisterRequest, CustomUserDetails customUserDetails) {

        Book book = bookRegisterRequest.toEntity(customUserDetails.getMemberId()); // DTO → Entity 변환
        book.setColor(BookHelper.getRandomColor()); // Book 객체에 랜덤(밝은 영역 위주) 색감 지정
        return bookRepository.save(book);
    }

    // ======================= 독서 포기 로직 =========================

    /* 독서 포기 메서드 */
    @Transactional
    public void giveUpReading(CustomUserDetails customUserDetails) {

        // 독서중인 도서가 없으면 "도서 없음" 오류 반환
        Book book = bookRepository.findByMemberIdAndStatus(customUserDetails.getMemberId(), Book.Status.독서중)
                .orElseThrow(() -> new MemberException(ErrorCode.BOOK_NOT_FOUND));

        book.setStatus(Book.Status.독서포기); // 독서 상태를 "독서중" -> "독서 포기"로 변환
        bookRepository.save(book); // 저장
    }

    // ======================= 독서 완료 영역 =========================

    /* 독서 완료 메서드 */
    @Transactional
    public List<CompleteResponse> completeReading(CustomUserDetails userDetails) {

        Book book = bookRepository.findByMemberIdAndStatus(userDetails.getMemberId(), Book.Status.독서중)
                .orElseThrow(() -> new BookException(ErrorCode.BOOK_NOT_FOUND));
        Member member = memberRepository.findByMemberId(userDetails.getMemberId())
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));

        List<String> emotionList = reviewRepository.findByBook_BookId(book.getBookId()).stream()
                .map(Review::getEmotion)
                .collect(Collectors.toList());

        int age = Period.between(member.getBirth(), LocalDate.now()).getYears();

        String prompt = BookHelper.buildPrompt(book, emotionList, member, age); // Helper 클래스에서 프롬프트 메시지 생성
        String aiResponse = chatClient.call(prompt); // AI 추천 도서 반환

        List<CompleteResponse> recommendations = BookHelper.parseRecommendations(aiResponse);

        /* Book 객체에서 "독서중" -> "독서 완료"로 정보 수정 */
        book.setStatus(Book.Status.독서완료);
        bookRepository.save(book);

        /* Member 객체에서 독서량(Completion) +1 */
        member.setCompletion(member.getCompletion() + 1);
        memberRepository.save(member);

        return recommendations;
    }

    // ======================= 장바구니 저장 영역 =========================

    /* 장바구니 자동 저장 메서드 */
    @Transactional
    public Cart saveAutoCart(CustomUserDetails userDetails, SaveBookAutoRequest request) {

        Member member = memberRepository.findByMemberId(userDetails.getMemberId())
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));

        Cart cart = BookHelper.createCartFromRequest(request, member); // helper 클래스 호출

        return cartRepository.save(cart);
    }

    // ======================= 캘린더에 선으로 표시하는 영역 =========================

    /* 등록된 도서를 캘린더에 선으로 표시 하는 메서드 */
    public List<PeriodResponse> getBooksPeriodInMonth(CustomUserDetails userDetails, PeriodRequest request) {

        int month = request.month();
        int year = LocalDate.now().getYear(); // 요청에서 연도도 받으려면 구조 확장 가능

        LocalDate start = BookHelper.getStartOfMonth(year, month);
        LocalDate end = BookHelper.getEndOfMonth(start);

        Integer memberId = userDetails.getMemberId();

        List<Book> books = bookRepository.findBooksInMonth(memberId, start, end);

        return books.stream()
                .map(book -> new PeriodResponse(
                        book.getBookId(),
                        book.getBookName(),
                        book.getStartDate(),
                        book.getFinishDate(),
                        book.getColor()))
                .toList();
    }

}
