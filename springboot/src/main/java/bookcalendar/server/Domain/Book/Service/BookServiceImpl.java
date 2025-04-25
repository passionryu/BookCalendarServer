package bookcalendar.server.Domain.Book.Service;

import bookcalendar.server.Domain.Book.DTO.Request.BookRegisterRequest;
import bookcalendar.server.Domain.Book.DTO.Request.PeriodRequest;
import bookcalendar.server.Domain.Book.DTO.Response.BookResponse;
import bookcalendar.server.Domain.Book.DTO.Response.CompleteResponse;
import bookcalendar.server.Domain.Book.DTO.Response.PeriodResponse;
import bookcalendar.server.Domain.Book.Entity.Book;
import bookcalendar.server.Domain.Book.Exception.BookException;
import bookcalendar.server.Domain.Book.Repository.BookRepository;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Exception.MemberException;
import bookcalendar.server.Domain.Member.Repository.MemberRepository;
import bookcalendar.server.Domain.Question.Repository.QuestionRepository;
import bookcalendar.server.Domain.Review.Entity.Review;
import bookcalendar.server.Domain.Review.Repository.ReviewRepository;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final QuestionRepository questionRepository;
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final ChatClient chatClient;

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
                    .orElseThrow(()->new MemberException(ErrorCode.BOOK_NOT_FOUND) );

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

    // ======================= 독서 포기 로직 =========================

    /**
     * 독서 포기 메서드
     *
     * @param customUserDetails
     */
    @Override
    @Transactional
    public void giveUpReading(CustomUserDetails customUserDetails) {

        // 1. 독서중인 도서 객체 반환
        Book book = bookRepository.findByMemberIdAndStatus(customUserDetails.getMemberId(), Book.Status.독서중)
                .orElseThrow(()->new MemberException(ErrorCode.BOOK_NOT_FOUND) );

        // 2. 상태를 '독서포기'로 변경
        book.setStatus(Book.Status.독서포기);

        // 3. 변경 사항 저장
        bookRepository.save(book);
    }

    // ======================= 독서 완료 로직 =========================

    /**
     * 독서 완료 메서드
     *
     * @param customUserDetails
     * @return
     */
    @Override
    @Transactional
    public  List<CompleteResponse>  completeReading(CustomUserDetails customUserDetails) {

        // 현재 독서 중인 도서 객체 반환
        Book book = bookRepository.findByMemberIdAndStatus(customUserDetails.getMemberId(), Book.Status.독서중)
                .orElseThrow(()-> new BookException(ErrorCode.BOOK_NOT_FOUND) );

        // 현재 독서 중인 도서의 독후감 객체 리스트 반환
        List<Review> reviews = reviewRepository.findByBook_BookId(book.getBookId());

        // 각 독후감 객체의 감정 데이터 리스트 반환
        List<String> emotionList = reviews.stream()
                .map(Review::getEmotion)
                .collect(Collectors.toList());

        // 현재 멤버 객체 반환
        Member member = memberRepository.findByMemberId(customUserDetails.getMemberId())
                .orElseThrow(()-> new MemberException(ErrorCode.USER_NOT_FOUND) );

        // 현재 유저의 나의 계산
        int age = Period.between(member.getBirth(), LocalDate.now()).getYears();

        // AI 프롬프트 (반드시 JSON 배열로 반환하도록 지시)
        String aiPromptMessage = String.format(
                """
                다음 정보를 참고해서 사용자에게 도서 5권을 추천해줘:
    
                - 읽은 책: "%s"
                - 장르: %s
                - 감정 목록: %s
                - 사용자 나이: %d살
                - 선호 장르: %s
                - 직업: %s
    
                아래 JSON 형식으로 꼭 반환해줘:
    
                [
                  {
                    "bookName": "책 제목",
                    "author": "저자 이름",
                    "reason": "이 도서를 추천하는 이유2~3줄"
                  },
                  ...
                ]
                """,
                book.getBookName(),
                book.getGenre(),
                emotionList,
                age,
                member.getGenre(),
                member.getJob()
        );

        String aiResponse = chatClient.call(aiPromptMessage);

        // 🛠 JSON 문자열을 CompleteResponse 리스트로 파싱
        ObjectMapper objectMapper = new ObjectMapper();
        List<CompleteResponse> recommendations;

        try {
            recommendations = objectMapper.readValue(
                    aiResponse,
                    new TypeReference<>() {}
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("AI 응답 파싱 실패: " + e.getMessage(), e);
        }

        // 독서 완료로 DB 변환 로직 추가
        book.setStatus(Book.Status.독서완료);
        bookRepository.save(book);

        return recommendations;

    }

    // ======================= 등록된 도서의 독서 기간을 캘린더에 선으로 표시하는 로직 =========================

    /**
     * 등록된 도서들을 캘린더에 선으로 표시하는 메서드
     *
     * @param customUserDetails
     * @param periodRequest
     * @return
     */
    @Override
    public List<PeriodResponse> getPeriodList(CustomUserDetails customUserDetails, PeriodRequest periodRequest) {

        int month = periodRequest.month();
        int year = LocalDate.now().getYear(); // 현재 연도 기준 (필요하면 요청 값으로 분리 가능)

        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        // 현재 사용자 ID 가져오기
        Integer memberId = customUserDetails.getMemberId();

        // 도서 기간이 해당 월과 겹치는 모든 도서 조회
        List<Book> books = bookRepository.findBooksInMonth(memberId, startOfMonth, endOfMonth);

        // Entity → DTO 매핑
        return books.stream()
                .map(book -> new PeriodResponse(book.getBookId() ,book.getBookName(), book.getStartDate(), book.getFinishDate()))
                .toList();
    }
}
