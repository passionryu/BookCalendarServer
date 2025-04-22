package bookcalendar.server.Domain.Review.Controller;

import bookcalendar.server.Domain.Review.DTO.Request.ReviewRequest;
import bookcalendar.server.Domain.Review.DTO.Response.QuestionResponse;
import bookcalendar.server.Domain.Review.Service.ReviewService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Review", description = "독후감 관리 API")
@RestController
@RequestMapping("/api/v1/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

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

}
