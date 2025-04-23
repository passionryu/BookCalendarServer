package bookcalendar.server.Domain.Review.Controller;

import bookcalendar.server.Domain.Book.Entity.Book;
import bookcalendar.server.Domain.Book.Repository.BookRepository;
import bookcalendar.server.Domain.Review.DTO.Request.ReviewRequest;
import bookcalendar.server.Domain.Review.DTO.Response.MainPageResponse;
import bookcalendar.server.Domain.Review.DTO.Response.QuestionResponse;
import bookcalendar.server.Domain.Review.DTO.Response.ReviewByDateResponse;
import bookcalendar.server.Domain.Review.Service.ReviewService;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.response.ApiResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@Slf4j
@Tag(name = "Review", description = "독후감 관리 API")
@RestController
@RequestMapping("/api/v1/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final BookRepository bookRepository;

    @Operation(summary = "독후감 작성  API", description = "독후감을 작성하고, AI 서비스에게 3가지의 질문을 반환한다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "독후감이 작성된 후, 정상적으로 AI 질문지가 반환되었습니다."),
                    @ApiResponse(responseCode = "401", description = "엑세스 토큰이 만료되었습니다."),
                    @ApiResponse(responseCode = "500", description = "서버 내부에서 오류가 발생했습니다.")
            })
    @PostMapping("/write")
    public ResponseEntity<ApiResponseWrapper<QuestionResponse>> writeReview(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                  @RequestBody ReviewRequest reviewRequest){

        // 독후감 작성 서비스 레이어 호츌
        QuestionResponse questionResponse = reviewService.writeReview(customUserDetails, reviewRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseWrapper<>(questionResponse,"독후감이 작성된 후, 정상적으로 AI 질문지가 반환되었습니다."));
    }


    @Operation(summary = "캘린더에서 날짜 선택 후 독후감 조회 API", description = "캘린더에서 선택한 날짜에 작성된 유저의 독후감, 질문&답변, AI 격려 메시지 반환",
            responses = {
                    @ApiResponse(responseCode = "201", description = "선택한 날짜의 독후감 기록이 정상적으로 반환되었습니다."),
                    @ApiResponse(responseCode = "401", description = "엑세스 토큰이 만료되었습니다."),
                    @ApiResponse(responseCode = "500", description = "서버 내부에서 오류가 발생했습니다.")
            })
    @GetMapping("/date")
    public ResponseEntity<ApiResponseWrapper<ReviewByDateResponse>> getReviewByDate(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                                    @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date){

        // 날짜에 따른 독후감 기록 조회 서비스 레이어 호출
        ReviewByDateResponse reviewByDateResponse = reviewService.getReviewByDate(customUserDetails,date);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(reviewByDateResponse,"선택한 날짜의 독후감 기록이 정상적으로 반환되었습니다."));
    }

    @Operation(summary = "메인페이지 독후감 진행률 & 남은 독서일 조회 API", description = "메인페이지 로딩시 독후감 진행률 & 남은 독서일 반환",
            responses = {
                    @ApiResponse(responseCode = "200", description = "메인페이지 독후감 진행률 & 남은 독서일이 정상적으로 반환되었습니다."),
                    @ApiResponse(responseCode = "401", description = "엑세스 토큰이 만료되었습니다."),
                    @ApiResponse(responseCode = "500", description = "서버 내부에서 오류가 발생했습니다.")
            })
    @GetMapping("/mainpage")
    public ResponseEntity<ApiResponseWrapper<MainPageResponse>> mainPage(@AuthenticationPrincipal CustomUserDetails customUserDetails){

        //현재 도서 중인 독서가 없으면 0 반환
        if (!bookRepository.existsByMemberIdAndStatus(customUserDetails.getMemberId(), Book.Status.독서중)) {
            return ResponseEntity.ok(
                    new ApiResponseWrapper<>(MainPageResponse.empty(), "현재 독서중인 도서가 없습니다.")
            );
        }

        //현재 도서 중인 독서가 있으면 서비스 레이어 호출
        MainPageResponse response = reviewService.mainPage(customUserDetails);
        return ResponseEntity.ok(
                new ApiResponseWrapper<>(response, "메인페이지 독후감 진행률 & 남은 독서일이 정상적으로 반환되었습니다.")
        );

    }
}

