package bookcalendar.server.Domain.Book.Controller;

import bookcalendar.server.Domain.Book.DTO.Request.BookRegisterRequest;
import bookcalendar.server.Domain.Book.Entity.Book;
import bookcalendar.server.Domain.Book.Service.BookService;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.response.ApiResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Book", description = "도서 관리 API")
@RestController
@RequestMapping("/api/v1/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * TODO : 독서중인 도서 정보 조회 기능 리펙토링
     *
     * 타입 안정성을 위한 Controller 레이어 Object 타입은 구체 타입으로 변환
     * 쿼리 2회를 1회로 줄일수 있는지 Service레이어 검토
     * 예외 안정성을 위한 Repository레이어 optinal 사용
     */

    /**
     * 독서중인 도서 정보 조회 API
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 등록 도서 정보 OR 도서 등록 페이지 유도 메시지
     */
    @Operation(summary = "독서중인 도서 정보 조회 API", description = " 현재 독서중인 도서가 DB에 존재하면 정보를 반환하고, 독서중인 도서가 없으면 도서 등록 유도 메시지를 반환한다.",
        responses = {
                @ApiResponse(responseCode = "200", description = "등록된 도서가 정상적으로 조회되었습니다."),
                @ApiResponse(responseCode = "200", description = "현재 독서중인 도서가 없습니다. 도서 등록이 필요합니다.")
        })
    @GetMapping("/info")
    public ResponseEntity<ApiResponseWrapper<Object>> bookInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails){

        // 등록 도서 존재 여부 확인 서비스 레이터 호출
        boolean existResult = bookService.bookExist(customUserDetails);

        if(existResult){
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponseWrapper<>(bookService.bookInfo(customUserDetails), "등록된 도서가 정상적으로 조회되었습니다."));
        }else{
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponseWrapper<>(null,"현재 독서중인 도서가 없습니다. 도서 등록이 필요합니다."));
        }
    }

    /**
     * TODO : 도서 등록 기능 리펙토링
     *
     * 오류 발생 가능성 있는 부분 확인하기
     */

    /**
     * 도서 등록 API
     *
     * @param bookRegisterRequest 도서 등록 데이터
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 등록 도서 데이터 + 도서 등록 성공 메시지
     */
    @Operation(summary = "도서 등록 API", description = "도서 등록에 필요한 모든 데이터를 입력 한 후 도서 등록 버튼을 누르면 동작하는 API이다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "입력하신 도서가 정상적으로 등록되었습니다.")
            })
    @PostMapping("")
    public ResponseEntity<ApiResponseWrapper<Book>> registerBook(@RequestBody BookRegisterRequest bookRegisterRequest,
                                                                 @AuthenticationPrincipal CustomUserDetails customUserDetails){

        // 도서 등록 서비스 레이어 호출
        Book book = bookService.registerBook(bookRegisterRequest,customUserDetails);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(book,"입력하신 도서가 정상적으로 등록되었습니다."));
    }

    /* 독서 포기 API */


}

