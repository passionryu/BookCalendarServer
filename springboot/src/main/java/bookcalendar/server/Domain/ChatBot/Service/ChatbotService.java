package bookcalendar.server.Domain.ChatBot.Service;

import bookcalendar.server.Domain.Book.DTO.Response.CompleteResponse;
import bookcalendar.server.Domain.ChatBot.DTO.Request.ChatRequest;
import bookcalendar.server.Domain.ChatBot.DTO.Request.SaveBookAutoRequest;
import bookcalendar.server.Domain.Mypage.Entity.Cart;
import bookcalendar.server.global.Security.CustomUserDetails;

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

    /**
     * 장바구니에 도서 자동 저장 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param saveBookAutoRequest 저장하고자 하는 도서의 정보 DTO
     * @return 장바구니 객체
     */
    Cart saveBookToCartByAuto(CustomUserDetails customUserDetails, SaveBookAutoRequest saveBookAutoRequest);
}
