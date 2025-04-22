package bookcalendar.server.Domain.Review.Controller;

import bookcalendar.server.Domain.Review.Service.ReviewService;
import bookcalendar.server.global.response.ApiResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Review", description = "독후감 관리 API")
@RestController
@RequestMapping("/api/v1/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "독후감 작성  API", description = " ",
            responses = {
                    @ApiResponse(responseCode = "200", description = "독후감이 정상적으로 저장되었습니다.")
            })
    @PostMapping("/write")
    public ResponseEntity<ApiResponseWrapper<String>> writeReview(){


        return null;
    }

}
