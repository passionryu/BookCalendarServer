package bookcalendar.server.Domain.Question.Controller;


import bookcalendar.server.Domain.Question.DTO.Request.QuestionAnswerRequest;
import bookcalendar.server.Domain.Question.Service.QuestionService;
import bookcalendar.server.Domain.Review.DTO.Response.AiResponse;
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
@Tag(name = "question", description = "질문지 작성 API")
@RestController
@RequestMapping("/api/v1/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    /**
     * 질문지 작성 API
     *
     * @param customUserDetails
     * @param questionAnswerRequest
     * @return
     */
    @Operation(summary = "질문지 작성  API", description = "",
            responses = {
                    @ApiResponse(responseCode = "200", description = "질문지에 대한 답변이 정상적으로 반환되었습니다."),
                    @ApiResponse(responseCode = "401", description = "엑세스 토큰이 만료되었습니다."),
                    @ApiResponse(responseCode = "500", description = "서버 내부에서 오류가 발생했습니다.")
            })
    @PostMapping("/write")
    public ResponseEntity<ApiResponseWrapper<AiResponse>> writeQuestion(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                        @RequestBody QuestionAnswerRequest questionAnswerRequest) {

        // 질문지 작성 서비스 레이어 호출
        AiResponse aiResponse = questionService.writeQuestion(customUserDetails, questionAnswerRequest);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(aiResponse,"질문지에 대한 답변이 정상적으로 반환되었습니다."));
    }

}
