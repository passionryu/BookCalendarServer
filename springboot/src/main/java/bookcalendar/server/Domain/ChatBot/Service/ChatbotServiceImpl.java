package bookcalendar.server.Domain.ChatBot.Service;

import bookcalendar.server.Domain.Book.DTO.Request.SaveBookAutoRequest;
import bookcalendar.server.Domain.Book.DTO.Response.CompleteResponse;
import bookcalendar.server.Domain.ChatBot.DTO.Request.SaveBookRequest;
import bookcalendar.server.Domain.ChatBot.Manager.RedisManager;
import bookcalendar.server.Domain.ChatBot.DTO.Request.ChatRequest;
import bookcalendar.server.Domain.ChatBot.Helper.RedisHelper;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Exception.MemberException;
import bookcalendar.server.Domain.Member.Repository.MemberRepository;
import bookcalendar.server.Domain.Mypage.Entity.Cart;
import bookcalendar.server.Domain.Mypage.Repository.CartRepository;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService{

    private final ChatClient chatClient;
    private final RedisManager redisManager;
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;

    // ======================= 챗봇 채팅 로직 =========================

    /**
     * 챗봇 채팅 메서드
     *
     * @param customUserDetails
     * @param chatRequest
     * @return
     */
    @Override
    public String chat(CustomUserDetails customUserDetails, ChatRequest chatRequest) {

        // Redis DB에서 기존의 대화내용이 있으면 반환
        String previousMessage  = redisManager.getPreviousMessage(customUserDetails.getMemberId());

        // 사용자 메시지를 프롬프팅
        String promptMessage = RedisHelper.makePromptMessage(chatRequest.chatMessage(), previousMessage);

        // 프로프팅한 유저의 채팅에 대한 AI 상담사 챗봇 답변 반환
        String aiResponse = chatClient.call(promptMessage);

        // AI 상담사 챗봇의 답변에 불순물 제거
        String cleanAiResponse = RedisHelper.textCleaner(aiResponse);

        // 유저 메시지,챗봇 답변 Redis에 업로드
        redisManager.saveMessageInRedis(customUserDetails.getMemberId(),chatRequest.chatMessage(),cleanAiResponse);

        // 최종적으로 AI 챗봇의 답변을 반환
        return cleanAiResponse;
    }

    // ======================= 챗봇 도서 추천 로직 =========================

    /**
     * 챗봇 도서 추천 메서드
     *
     * @param customUserDetails
     * @return
     */
    @Override
    @Transactional
    public List<CompleteResponse> recommend(CustomUserDetails customUserDetails) {

        // 모든 메시지 반환
        String everyMessages = redisManager.getAllMessage(customUserDetails.getMemberId());

        // 도서추천을 위한 대화 내용을 AI 프롬프팅 (반드시 JSON 배열로 반환하도록 지시)
        String aiPromptMessage = RedisHelper.getPromptMessage(everyMessages);

        // 프롬프팅 메시지를 AI 챗봇에게 전송
        String aiResponse = chatClient.call(aiPromptMessage);

        // AI 챗봇이 답변한 추천도서 5개를 파싱
        List<CompleteResponse> recommendations = RedisHelper.parseJsonArray(aiResponse);

        // Redis에 저장된 모든 대화 메시지 삭제
        redisManager.deleteAllMessages(customUserDetails.getMemberId());

        // 추천 도서 5개 반환
        return recommendations;
    }

    /**
     * 장바구니에 도서 자동 저장 메서드
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param saveBookAutoRequest 저장하고자 하는 도서의 정보 DTO
     * @return
     */
    @Override
    @Transactional
    public Cart saveBookToCartByAuto(CustomUserDetails customUserDetails, SaveBookRequest saveBookAutoRequest) {
        // 현재 멤버 객체 반환
        Member member = memberRepository.findByMemberId(customUserDetails.getMemberId())
                .orElseThrow(()-> new MemberException(ErrorCode.USER_NOT_FOUND) );

        // cart 객체 생성
        Cart cart = Cart.builder()
                .bookName(saveBookAutoRequest.bookName())
                .author(saveBookAutoRequest.author())
                .link(saveBookAutoRequest.url())
                .date(LocalDateTime.now())
                .member(member)
                .build();

        return cartRepository.save(cart);
    }


}
