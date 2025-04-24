package bookcalendar.server.Domain.ChatBot.Controller;


import bookcalendar.server.Domain.Book.DTO.Response.CompleteResponse;
import bookcalendar.server.Domain.ChatBot.DTO.Request.ChatRequest;
import bookcalendar.server.Domain.ChatBot.Service.ChatbotService;
import bookcalendar.server.Domain.Member.DTO.Request.RegisterRequest;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.response.ApiResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "Chatbot", description = "챗봇 API")
@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
public class ChatBotController {

    private final ChatbotService chatbotService;

    /**
     * 챗봇 채팅 API
     *
     * @param customUserDetails
     * @param chatRequest
     * @return
     */
    @Operation(summary = "챗봇 채팅 API", description = "String값으로 채팅 데이터를 전송받고 AI답변을 반환",
            responses = {
                    @ApiResponse(responseCode = "200", description = "채팅 메시지가 정상적으로 전송/반환되었습니다."),
                    @ApiResponse(responseCode = "409", description = "")
            })
    @PostMapping("/chat")
    public ResponseEntity<ApiResponseWrapper<String>> chat(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                          @RequestBody ChatRequest chatRequest) {

        // 챗봇 채팅 서비스 레이어 호출
        String aiResponse = chatbotService.chat(customUserDetails, chatRequest);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(aiResponse, "채팅 메시지가 정상적으로 전송/반환되었습니다."));
    }

    /**
     * 도서 추천 API
     *
     * @param customUserDetails
     * @return
     */
    @Operation(summary = "도서 추천 API", description = "",
            responses = {
                    @ApiResponse(responseCode = "200", description = "도서 추천이 정상적으로 반환되었습니다."),
                    @ApiResponse(responseCode = "409", description = "")
            })
    @GetMapping("/recommend")
    public ResponseEntity<ApiResponseWrapper<List<CompleteResponse>>> chat(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        // 도서 추천 서비스 레이어 호출
        List<CompleteResponse> completeResponseList = chatbotService.recommend(customUserDetails);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(completeResponseList, "도서 추천이 정상적으로 반환되었습니다."));
    }

}
