package bookcalendar.server.Domain.Book.Controller;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Book", description = "도서 관리 API")
@RestController
@RequestMapping("/api/v1/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * TODO : 리펙토링
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

        boolean existResult = bookService.bookExist(customUserDetails);

        if(existResult){
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ApiResponseWrapper<>(bookService.bookInfo(customUserDetails), "등록된 도서가 정상적으로 조회되었습니다."));
        } else {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ApiResponseWrapper<>(null,"현재 독서중인 도서가 없습니다. 도서 등록이 필요합니다."));
        }
    }
}

