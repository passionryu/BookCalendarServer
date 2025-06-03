package bookcalendar.server.Domain.ChatBot.Service;

import bookcalendar.server.Domain.Book.DTO.Response.CompleteResponse;
import bookcalendar.server.Domain.ChatBot.DTO.Request.ChatRequest;
import bookcalendar.server.Domain.ChatBot.DTO.Request.SaveBookRequest;
import bookcalendar.server.Domain.Mypage.Entity.Cart;
import bookcalendar.server.global.Security.CustomUserDetails;

import java.util.List;

public interface ChatbotService {

    /**
     * 향상된 챗봇 서비스 인터페이스
     *
     * 1. Redis-Session에 fastapi관련 에러기록이 있는지 확인
     * 2. 에러 기록 없으면 fastapi 호출 || 에러 기록 있으면 바로 Gpt 모델로 연결
     * 3. fastapi 호출간에 에러 반환 시, Gpt호출 && Redis에 보고 && fastapi 재시작 명령어 호출
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param chatRequest 클라이언트의  채팅 내용
     * @return 답변 내용
     */
    String enhancedChat(CustomUserDetails customUserDetails, ChatRequest chatRequest);

    /* AI 챗봇 모델 연결 인터페이스 */
    // String aiChat(CustomUserDetails customUserDetails, ChatRequest chatRequest);

    /**
     * 챗봇 채팅 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param chatRequest 채팅 내용
     * @return 답변 내용
     */
    String chat(CustomUserDetails customUserDetails, ChatRequest chatRequest);

    /**
     * 도서 추천 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 추천 도서 리스트
     */
    List<CompleteResponse> recommend(CustomUserDetails customUserDetails);

    /**
     * 장바구니에 도서 자동 저장 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param saveBookAutoRequest 저장하고자 하는 도서의 정보 DTO
     * @return 장바구니 객체
     */
    Cart saveBookToCartByAuto(CustomUserDetails customUserDetails, SaveBookRequest saveBookAutoRequest);
}
