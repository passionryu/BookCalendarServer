package bookcalendar.server.Domain.Book.Controller;

import bookcalendar.server.Domain.Book.DTO.Request.BookRegisterRequest;
import bookcalendar.server.Domain.Book.DTO.Request.PeriodRequest;
import bookcalendar.server.Domain.Book.DTO.Request.SaveBookAutoRequest;
import bookcalendar.server.Domain.Book.DTO.Response.BookResponse;
import bookcalendar.server.Domain.Book.DTO.Response.CompleteResponse;
import bookcalendar.server.Domain.Book.DTO.Response.PeriodResponse;
import bookcalendar.server.Domain.Book.Entity.Book;
import bookcalendar.server.Domain.Book.Service.BookService;
import bookcalendar.server.Domain.Mypage.Entity.Cart;
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

import java.util.List;

@Slf4j
@Tag(name = "Book", description = "도서 관리 API")
@RestController
@RequestMapping("/api/v1/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

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

        long start = System.currentTimeMillis(); // 시간 측정 시작

        boolean existResult = bookService.bookExist(customUserDetails);
        ApiResponseWrapper<Object> response;

        if (existResult) {
            BookResponse bookResponse = bookService.bookInfo(customUserDetails); // 캐싱이 적용된 메서드
            response = new ApiResponseWrapper<>(bookResponse, "등록된 도서가 정상적으로 조회되었습니다.");
        } else {
            response = new ApiResponseWrapper<>(null, "현재 독서중인 도서가 없습니다. 도서 등록이 필요합니다.");
        }

        long end = System.currentTimeMillis(); // 시간 측정 종료
        long duration = end - start;
        log.info("[bookInfo] 처리 시간: {}ms", duration); // 로그 출력

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 도서 등록 API
     *
     * @param bookRegisterRequest 도서 등록 데이터
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 등록 도서 데이터 + 도서 등록 성공 메시지
     */
    @Operation(summary = "도서 등록 API", description = "도서 등록에 필요한 모든 데이터를 입력 한 후 도서 등록 버튼을 누르면 동작하는 API이다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "입력하신 도서가 정상적으로 등록되었습니다."),
                    @ApiResponse(responseCode = "401", description = "유저 인증 오류"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @PostMapping("")
    public ResponseEntity<ApiResponseWrapper<Book>> registerBook(@RequestBody BookRegisterRequest bookRegisterRequest,
                                                                 @AuthenticationPrincipal CustomUserDetails customUserDetails){

        // 도서 등록 서비스 레이어 호출
        Book book = bookService.registerBook(bookRegisterRequest,customUserDetails);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(book,"입력하신 도서가 정상적으로 등록되었습니다."));
    }

    /**
     * 독서 포기 API
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 독서 포기 메시지
     */
    @Operation(summary = "독서 포기 API", description = "독서 포기 버튼 클릭 시, DB에서 해당 도서의 status 칼럼이 (독서_포기)로 전환된다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "요청하신 도서에 대하여 포기 처리가 완료되었습니다."),
                    @ApiResponse(responseCode = "401", description = "유저 인증 오류"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @PatchMapping("")
    public ResponseEntity<ApiResponseWrapper<Void>> giveUpReading(@AuthenticationPrincipal CustomUserDetails customUserDetails){

        //독서 포기 서비스 레이어 호출
        bookService.giveUpReading(customUserDetails);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(null, "요청하신 도서에 대하여 포기 처리가 완료되었습니다."));
    }

    /**
     * 독서 완료 API
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 5개의 도서 추천
     */
    @Operation(summary = "독서 완료 API", description = "독서 완료 버튼 클릭 시 , 도서 추천 5권 및 DB에서 해당 도서의 (status= 독서 완료)로 수정 ",
            responses = {
                    @ApiResponse(responseCode = "200", description = "요청하신 도서에 대하여 독서 완료 처리가 완료되었습니다."),
                    @ApiResponse(responseCode = "401", description = "유저 인증 오류"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @PostMapping("/complete")
    public ResponseEntity<ApiResponseWrapper<List<CompleteResponse>>> completeReading(@AuthenticationPrincipal CustomUserDetails customUserDetails){

        // 독서 완료 서비스 레이어 호출
        List<CompleteResponse> completeResponses = bookService.completeReading(customUserDetails);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(completeResponses,"요청하신 도서에 대하여 독서 완료 처리가 완료되었습니다."));
    }

    /**
     * 독서 완료를 통한 도서 추천에서 장바구니에 책 저장 API
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param saveBookAutoRequest 도서 장바구니 저장 정보 DTO
     * @return 도서 저장 성공 메시지
     */
    @Operation(summary = "독서 완료를 통한 도서 추천에서 장바구니에 책 저장 API", description = "독서 완료를 통한 도서 추천 시 장바구니에 저장할 수 있는 기능이다. ",
            responses = {
                    @ApiResponse(responseCode = "200", description = "해당 도서를 정상적으로 장바구니에 저장했습니다."),
                    @ApiResponse(responseCode = "401", description = "유저 인증 오류"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @PostMapping("/cart")
    public ResponseEntity<ApiResponseWrapper<Cart>> saveBookToCartByAuto(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                         @RequestBody SaveBookAutoRequest saveBookAutoRequest){

        // 추천받은 도서 자동 장바구니 저장 서비스 레이어 호출
        Cart cart = bookService.saveBookToCartByAuto(customUserDetails,saveBookAutoRequest);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(cart,"해당 도서를 정상적으로 장바구니에 저장했습니다."));
    }

    /**
     *  등록된 도서리스트를 메인페이지에 표시 하는 API
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param periodRequest 달 (예 :1월, 2월)
     * @return 캘린더에 도서 기간 표시를 할 수 있는 리스트
     */
    @Operation(summary = " 등록된 도서리스트를 메인페이지에 표시 하는 API", description = " 등록된 도서리스트를 메인페이지에 표시하는 api이다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "이번 달에 등록된 모든 도서의 기간이 반환되었습니다."),
                    @ApiResponse(responseCode = "401", description = "유저 인증 오류"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @PostMapping("/period")
    public ResponseEntity<ApiResponseWrapper<List<PeriodResponse>>> getPeriodList(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                                  @RequestBody PeriodRequest periodRequest){

        long start = System.currentTimeMillis(); // 시간 측정 시작

        // 등록된 도서들의 도서 기간을 캘린더에 반환하는 서비스 레이어 호출
        List<PeriodResponse> periodResponseList = bookService.getPeriodList(customUserDetails,periodRequest);

        long end = System.currentTimeMillis(); // 시간 측정 종료
        long duration = end - start;
        log.info("[List<PeriodResponse>] 처리 시간: {}ms", duration); // 로그 출력

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(periodResponseList,"이번 달에 등록된 모든 도서의 기간이 반환되었습니다."));
    }

}

