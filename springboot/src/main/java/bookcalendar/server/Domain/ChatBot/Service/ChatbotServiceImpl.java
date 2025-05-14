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
import bookcalendar.server.global.Aladin.AladinResponse;
import bookcalendar.server.global.Aladin.AladinService;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService{

    private final ChatClient chatClient;
    private final RedisManager redisManager;
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final AladinService aladinService;

    // ======================= 챗봇 채팅 로직 =========================

    /* 챗봇 채팅 메서드 */
    @Override
    public String chat(CustomUserDetails customUserDetails, ChatRequest chatRequest) {

        String previousMessage  = redisManager.getPreviousMessage(customUserDetails.getMemberId()); // Redis DB에서 기존의 대화내용이 있으면 반환
        String promptMessage = RedisHelper.makePromptMessage(chatRequest.chatMessage(), previousMessage); // 사용자 메시지를 프롬프팅
        String aiResponse = chatClient.call(promptMessage); // 프로프팅한 유저의 채팅에 대한 AI 상담사 챗봇 답변 반환
        String cleanAiResponse = RedisHelper.textCleaner(aiResponse); // AI 상담사 챗봇의 답변에 불순물 제거

        redisManager.saveMessageInRedis(customUserDetails.getMemberId(),chatRequest.chatMessage(),cleanAiResponse); // 유저 메시지,챗봇 답변 Redis에 업로드

        return cleanAiResponse;
    }

    // ======================= 챗봇 도서 추천 로직 =========================

    /* 챗봇 도서 추천 메서드 */
    @Override
    @Transactional
    public List<CompleteResponse> recommend(CustomUserDetails customUserDetails) {


        String everyMessages = redisManager.getAllMessage(customUserDetails.getMemberId());  // Redis 메모리에서 모든 메시지 반환
        String aiPromptMessage = RedisHelper.getPromptMessage(everyMessages); // 도서추천을 위한 대화 내용을 AI 프롬프팅 (반드시 JSON 배열로 반환하도록 지시)

        String aiResponse = chatClient.call(aiPromptMessage); // 프롬프팅 메시지를 AI 챗봇에게 전송
        List<CompleteResponse> recommendations = RedisHelper.parseJsonArray(aiResponse); // AI 챗봇이 답변한 추천도서 5개를 파싱

        // 알라딘 API로 각 도서의 URL 가져오기
        recommendations = recommendations.stream().map(response -> {
            try {
                AladinResponse aladinResponse = aladinService.searchBook(response.getBookName());
                return new CompleteResponse(
                        response.getBookName(),
                        response.getAuthor(),
                        response.getReason(),
                        aladinResponse.url()
                );
            } catch (Exception e) {
                // 알라딘 API 호출 실패 시 URL을 빈 문자열로 설정
                return new CompleteResponse(
                        response.getBookName(),
                        response.getAuthor(),
                        response.getReason(),
                        ""
                );
            }
        }).collect(Collectors.toList());

        redisManager.deleteAllMessages(customUserDetails.getMemberId()); // Redis에 저장된 모든 대화 메시지 삭제

        return recommendations;
    }

    /* 추천 도서 장바구니에 저장 메서드 */
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
