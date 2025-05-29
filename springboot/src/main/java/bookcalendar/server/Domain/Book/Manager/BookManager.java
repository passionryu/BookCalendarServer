package bookcalendar.server.Domain.Book.Manager;

import bookcalendar.server.Domain.Book.DTO.Request.BookRegisterRequest;
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
import bookcalendar.server.global.BookOpenApi.Aladin.AladinResponse;
import bookcalendar.server.global.BookOpenApi.Aladin.AladinService;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookManager {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final ReviewRepository reviewRepository;
    private final AladinService aladinService;

    private final ChatClient chatClient;
    private final CacheManager cacheManager;

    private static final Set<String> INVALID_TOPICS = Set.of("책", "도서", "서적", "추천", "중고", "알라딘",
            "포장팩", "가방", "쇼핑", "상품", "판매");

    // ======================= Util 코드 =========================

    /* 현재 유저 객체 반환 */
    public Member getmember(Integer memberId) {
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));
    }

    /* 독서 중인 도서 반환 */
    public Book getRedaingBook(Integer memberId) {
        return  bookRepository.findByMemberIdAndStatus(memberId, Book.Status.독서중)
                .orElseThrow(() -> new BookException(ErrorCode.BOOK_NOT_FOUND));

    }

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

    /* 도서 등록 시  getPeriodList 캐시 삭제 메서드 */
    public void evictMonthlyBookListCache(Integer memberId) {
        for (int month = 1; month <= 12; month++) {
            String key = memberId + "-" + month;
            cacheManager.getCache("monthlyBookList").evictIfPresent(key);
        }
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
    public List<CompleteResponse> completeReading(Member member, Book book) {

        // 해당 도서에 대한 독후감 리스트를 반환
        List<String> contentsList = reviewRepository.findByBook_BookId(book.getBookId()).stream()
                .map(Review::getContents)
                .toList();

        // 유저의 나이 반환
        int age = Period.between(member.getBirth(), LocalDate.now()).getYears();

        // Input 정보에서 2개의 메인 주제를 추출
        List<String> topicList = getTopicsFromInformation(book,contentsList,member,age).stream()
                .filter(topic -> !INVALID_TOPICS.contains(topic)) // 주제로 나오면 안되는 블랙리스트 운영
                .collect(Collectors.toList());

        // 2개의 주제로 3+2=5 권 추천
        return getBookFromAladin(topicList);
    }

    /* Input 정보에서 2개의 메인 주제를 추출 */
    private List<String> getTopicsFromInformation(Book book, List<String> contentsList, Member member, int age){

        /* Input 정보에서 2개의 메인 주제를 추출 */
        String prompt = BookHelper.buildPrompt(book, contentsList, member, age); // Helper 클래스에서 프롬프트 메시지 생성
        String aiResponse = chatClient.call(prompt); // AI가 2가지의 주제 반환
        List<String> topicList = BookHelper.parseTopicList(aiResponse); // AI의 답변을 파싱
        log.info("getTopicList메서드 결과 : {}", topicList);

        return topicList;
    }

    /* 알라딘에서 5권의 도서를 추출 */
    private List<CompleteResponse> getBookFromAladin(List<String> topicList){

        List<CompleteResponse> recommendations = new ArrayList<>();

        // 주제1로 책 3권 추천
        List<Optional<CompleteResponse>> topic1Books = aladinService.searchTopBooksByTopic(topicList.get(0), 3);
        topic1Books.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(recommendations::add);

        // 주제2로 책 2권 추천
        List<Optional<CompleteResponse>> topic2Books = aladinService.searchTopBooksByTopic(topicList.get(1), 2);
        topic2Books.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(recommendations::add);

        return recommendations;
    }

    // ======================= 장바구니 저장 영역 =========================

    /* 장바구니 자동 저장 메서드 */
    @Transactional
    public Cart saveAutoCart(CustomUserDetails userDetails, SaveBookAutoRequest request) {

        Member member = getmember(userDetails.getMemberId());

        Cart cart = BookHelper.createCartFromRequest(request, member); // helper 클래스 호출

        return cartRepository.save(cart);
    }

    // ======================= 캘린더에 선으로 표시하는 영역 =========================

    /* 등록된 도서리스트 반환 메서드  */
    public List<PeriodResponse> getBooksPeriodInMonth(Integer memberId, LocalDate start, LocalDate end) {

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
