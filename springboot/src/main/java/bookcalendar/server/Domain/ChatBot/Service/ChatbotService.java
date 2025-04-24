package bookcalendar.server.Domain.ChatBot.Service;

import bookcalendar.server.Domain.Book.DTO.Response.CompleteResponse;
import bookcalendar.server.Domain.ChatBot.DTO.Request.ChatRequest;
import bookcalendar.server.global.Security.CustomUserDetails;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface ChatbotService {

    /**
     * 챗봇 채팅 인터페이스
     *
     * @param customUserDetails
     * @param chatRequest
     * @return
     */
    String chat(CustomUserDetails customUserDetails, ChatRequest chatRequest);

    /**
     * 도서 추천 인터페이스
     *
     * @param customUserDetails
     * @return
     */
    List<CompleteResponse> recommend(CustomUserDetails customUserDetails);
}
