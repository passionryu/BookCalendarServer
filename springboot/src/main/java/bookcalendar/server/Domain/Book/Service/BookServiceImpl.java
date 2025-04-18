package bookcalendar.server.Domain.Book.Service;

import bookcalendar.server.Domain.Book.DTO.Request.BookRegisterRequest;
import bookcalendar.server.Domain.Book.DTO.Response.BookResponse;
import bookcalendar.server.Domain.Book.Entity.Book;
import bookcalendar.server.Domain.Book.Repository.BookRepository;
import bookcalendar.server.global.Security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    // ======================= 독서중인 도서 정보 반환 로직 =========================

    /**
     * 현재 독서중인 도서 존재 여부 확인
     *
     * @param customUserDetails 인증된 유저의 객체 정보
     * @return True False
     */
    @Override
    public boolean bookExist(CustomUserDetails customUserDetails) {

        // 현재 DB에서 독서중인 도서가 있는지 확인 후 반환
        return bookRepository.existsByMemberIdAndStatus(
                customUserDetails.getMemberId(),
                Book.Status.독서중);
    }

    /**
     * 도서정보 반환 메서드
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 도서 정보
     */
    @Override
    public BookResponse bookInfo(CustomUserDetails customUserDetails) {

            // 독서중인 도서 객체 반환
            Book book = bookRepository.findByMemberIdAndStatus(
                    customUserDetails.getMemberId(),
                    Book.Status.독서중);

            // 도서 정보 조회 페이지에 필요한 정보 DTO 패키징 후 반환
            return new BookResponse(
                    book.getBookName(),
                    book.getAuthor(),
                    book.getTotalPage(),
                    book.getGenre(),
                    book.getStartDate(),
                    book.getFinishDate());
    }

    // ======================= 도서 등록 로직 =========================

    /**
     * 도서 등록 메서드
     *
     * @param bookRegisterRequest 도서 등록 데이터
     * @return 등록 도서 데이터
     */
    @Override
    @Transactional
    public Book registerBook(BookRegisterRequest bookRegisterRequest,CustomUserDetails customUserDetails) {

        // 유저의 정보 객체에서 memberId 추출
        Integer memberId = customUserDetails.getMemberId();

        // 도서를 객체화 하여 저장 후 결과 반환
        return bookRegisterRequest.toEntity(memberId);
    }
}
