package bookcalendar.server.Domain.Book.Service;

import bookcalendar.server.Domain.Book.DTO.Request.BookRegisterRequest;
import bookcalendar.server.Domain.Book.DTO.Response.BookResponse;
import bookcalendar.server.Domain.Book.Entity.Book;
import bookcalendar.server.Domain.Book.Exception.BookException;
import bookcalendar.server.Domain.Book.Repository.BookRepository;
import bookcalendar.server.Domain.Member.Exception.MemberException;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.exception.ErrorCode;
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
            Book book = bookRepository.findByMemberIdAndStatus(customUserDetails.getMemberId(), Book.Status.독서중)
                    .orElseThrow(()->new MemberException(ErrorCode.USER_NOT_FOUND) );

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

        /* 만약 DB에 "독서중"인 도서 조회시 에러 메시지 반환*/
        if(bookRepository.existsByMemberIdAndStatus(
                customUserDetails.getMemberId(),
                Book.Status.독서중))
            throw new BookException(ErrorCode.READING_BOOK_ALREADY_EXIST);

        // 입력 DTO를 Entity로 전환
        Book book = bookRegisterRequest.toEntity(customUserDetails.getMemberId());

        // 저장 및 book 객체 반환
        return bookRepository.save(book);
    }
}
