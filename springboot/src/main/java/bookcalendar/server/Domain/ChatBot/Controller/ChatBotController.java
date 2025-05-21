package bookcalendar.server.Domain.ChatBot.Controller;


import bookcalendar.server.Domain.Book.DTO.Response.CompleteResponse;
import bookcalendar.server.Domain.ChatBot.DTO.Request.ChatRequest;
import bookcalendar.server.Domain.ChatBot.DTO.Request.SaveBookRequest;
import bookcalendar.server.Domain.ChatBot.Service.ChatbotService;
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
@Tag(name = "Chatbot", description = "챗봇 API")
@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
public class ChatBotController {

    private final ChatbotService chatbotService;

    /**
     * 챗봇 채팅 API
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param chatRequest 유저의 채팅 메시지
     * @return AI 챗봇의 반환 메시지
     */
    @Operation(summary = "챗봇 채팅 API", description = "String값으로 채팅 데이터를 전송받고 AI답변을 반환",
            responses = {
                    @ApiResponse(responseCode = "200", description = "채팅 메시지가 정상적으로 전송/반환되었습니다."),
                    @ApiResponse(responseCode = "409", description = ""),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @PostMapping("/chat")
    public ResponseEntity<ApiResponseWrapper<String>> chat(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                          @RequestBody ChatRequest chatRequest) {

        // Fast-API 서버 ChatBot AI 연결 서비스 레이어 호출
        String aiResponse = chatbotService.aiChat(customUserDetails, chatRequest);
        // 챗봇 채팅 서비스 레이어 호출
        //String aiResponse = chatbotService.chat(customUserDetails, chatRequest);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(aiResponse, "채팅 메시지가 정상적으로 전송/반환되었습니다."));
    }

    /**
     * 도서 추천 API
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return
     */
    @Operation(summary = "도서 추천 API", description = "챗봇 대화 데이터를 기반으로 추천 도서 리스트 5권을 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "도서 추천이 정상적으로 반환되었습니다."),
                    @ApiResponse(responseCode = "409", description = ""),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @GetMapping("/recommend")
    public ResponseEntity<ApiResponseWrapper<List<CompleteResponse>>> chat(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        // 도서 추천 서비스 레이어 호출
        List<CompleteResponse> completeResponseList = chatbotService.recommend(customUserDetails);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(completeResponseList, "도서 추천이 정상적으로 반환되었습니다."));
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
                                                                         @RequestBody SaveBookRequest saveBookAutoRequest){

        // 추천받은 도서 자동 장바구니 저장 서비스 레이어 호출
        Cart cart = chatbotService.saveBookToCartByAuto(customUserDetails,saveBookAutoRequest);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(cart,"해당 도서를 정상적으로 장바구니에 저장했습니다."));
    }



}
