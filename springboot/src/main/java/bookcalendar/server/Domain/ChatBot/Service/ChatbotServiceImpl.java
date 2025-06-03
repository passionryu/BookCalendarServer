package bookcalendar.server.Domain.ChatBot.Service;

import bookcalendar.server.Domain.Book.DTO.Response.CompleteResponse;
import bookcalendar.server.Domain.ChatBot.DTO.Request.ChatRequest;
import bookcalendar.server.Domain.ChatBot.DTO.Request.SaveBookRequest;
import bookcalendar.server.Domain.ChatBot.Helper.ChatBotHelper;
import bookcalendar.server.Domain.ChatBot.Manager.ChatBotManager;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Exception.MemberException;
import bookcalendar.server.Domain.Member.Repository.MemberRepository;
import bookcalendar.server.Domain.Mypage.Entity.Cart;
import bookcalendar.server.Domain.Mypage.Repository.CartRepository;
import bookcalendar.server.global.ExternalConnection.Client.IntentClient;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService{

    private final ChatClient chatClient;
    private final ChatBotManager chatBotManager;
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final IntentClient intentClient;

    @Qualifier("sessionRedisTemplate")
    private final RedisTemplate<String, String> sessionRedisTemplate;

   // ======================= AI 채팅 로직 =========================

    /* 향상된 채팅 서비스 - fastapi와 gpt 혼용*/
    @Override
    public String enhancedChat(CustomUserDetails customUserDetails, ChatRequest chatRequest) {

     /* 1. Redis-Session에 fastapi관련 에러기록이 있는지 확인 */

        // Redis-Session에 fastapi 관련 에러 기록이 있는지 확인
        String redisKey = "FastAPI-Error";
        String aiResponse;
        String errorFlag = sessionRedisTemplate.opsForValue().get(redisKey);

     /* 2. 에러 기록 없으면 fastapi 호출 || 에러 기록 있으면 바로 Gpt 모델로 연결 */

        // Fast- API서버 다운 시 Gpt 모델 호출
        if (errorFlag != null) {
            log.info("fastapi 오류 기록 확인 {}, Gpt모델로 임시 대체", errorFlag);

            String previousMessage  = chatBotManager.getPreviousMessage(customUserDetails.getMemberId()); // Redis DB에서 기존의 대화내용이 있으면 반환
            String promptMessage = ChatBotHelper.makePromptMessage(chatRequest.chatMessage(), previousMessage); // 사용자 메시지를 프롬프팅
            String rawAiResponse = chatClient.call(promptMessage); // 프로프팅한 유저의 채팅에 대한 AI 상담사 챗봇 답변 반환
            aiResponse = ChatBotHelper.textCleaner(rawAiResponse); // AI 상담사 챗봇의 답변에 불순물 제거
        }else{
            // 3. FastAPI 시도 → 실패 시 GPT 호출 및 Redis에 에러 기록 (TTL 30분)
            try {
                aiResponse = intentClient.predict(chatRequest.chatMessage()).block();
            } catch (Exception e) {
     /* 3. fastapi 호출간에 에러 반환 시, Gpt호출 && Redis에 보고 && fastapi 재시작 명령어 호출 */

                log.info("FastAPI 예외 발생 & Gpt모델 호출 전환 - Fast-API 에러메시지 :{}", e.getMessage());

                // todo : get errorTime
                // 에러 발생 시간을 에러키의 Value로 대입
                LocalDateTime errorTime = LocalDateTime.now();
                String errorTimeStr = errorTime.toString();

                // Redis에 FastAPI 오류 보고 (TTL: 30분)
                sessionRedisTemplate.opsForValue().set(redisKey, errorTimeStr, Duration.ofMinutes(30));

                // Gpt 모델 호출
                String previousMessage = chatBotManager.getPreviousMessage(customUserDetails.getMemberId());
                String promptMessage = ChatBotHelper.makePromptMessage(chatRequest.chatMessage(), previousMessage);
                String rawAiResponse = chatClient.call(promptMessage);
                aiResponse = ChatBotHelper.textCleaner(rawAiResponse);

                // todo : reRunFast-API
                // fast-api 재시작 명령어 가동
                // 이 명령을 bash script로 만들어 놓고, Java에서 sh restart_fastapi.sh 실행하도록 해도 관리가 더 편리할 수 있을 것 같음.
                try {
                    ProcessBuilder builder = new ProcessBuilder(
                            "/usr/bin/bash", "-c", "uvicorn main:app --host 0.0.0.0 --port 3004 --reload"
                    );
                    builder.directory(new File("/home/t25101/v0.5/ai/BookCalendar-AI")); // FastAPI가 위치한 디렉토리
                    builder.start();
                    log.info("FastAPI 재시작 명령 실행 완료");
                } catch (IOException e2) {
                    log.info("FastAPI 재시작 실패", e2);
                }
            }
        }

        // 유저 메시지,챗봇 답변 Redis에 업로드
        chatBotManager.saveMessageInRedis(customUserDetails.getMemberId(), chatRequest.chatMessage(), aiResponse);

        return aiResponse;
    }

    /* Gpt Mock AI 챗봇 채팅 메서드 */
    @Override
    public String chat(CustomUserDetails customUserDetails, ChatRequest chatRequest) {

        /* Fast-API 챗봇 AI 호출 */
        // String aiResponse = intentClient.predict(chatRequest.chatMessage()).block(); // fast-api 서버의 ai 챗봇 모델 호출

        /* Fast- API서버 다운 시 Gpt 모델 호출 */
        String previousMessage  = chatBotManager.getPreviousMessage(customUserDetails.getMemberId()); // Redis DB에서 기존의 대화내용이 있으면 반환
        String promptMessage = ChatBotHelper.makePromptMessage(chatRequest.chatMessage(), previousMessage); // 사용자 메시지를 프롬프팅
        String rawAiResponse = chatClient.call(promptMessage); // 프로프팅한 유저의 채팅에 대한 AI 상담사 챗봇 답변 반환
        String aiResponse = ChatBotHelper.textCleaner(rawAiResponse); // AI 상담사 챗봇의 답변에 불순물 제거

        chatBotManager.saveMessageInRedis(customUserDetails.getMemberId(),chatRequest.chatMessage(),aiResponse); // 유저 메시지,챗봇 답변 Redis에 업로드

        return aiResponse;
    }

      /* Fast-API AI서버의 AI 챗봇 채팅 로직 */
//    @Override
//    public String aiChat(CustomUserDetails customUserDetails, ChatRequest chatRequest) {
//
//        String aiResponse = intentClient.predict(chatRequest.chatMessage()).block(); // fast-api 서버의 ai 챗봇 모델 호출
//        chatBotManager.saveMessageInRedis(customUserDetails.getMemberId(), chatRequest.chatMessage(),aiResponse); // 유저 메시지,챗봇 답변 Redis에 업로드
//
//        return aiResponse;
//    }

    // ======================= 챗봇 도서 추천 로직 =========================

    /* 챗봇 도서 추천 메서드 */
    @Override
    @Transactional
    public List<CompleteResponse> recommend(CustomUserDetails customUserDetails) {

        String topic = chatBotManager.getTopicsFromMessages(customUserDetails);  // 채팅 내용에서 1개의 주제 추출
        List<CompleteResponse> recommendations = chatBotManager.getBookFromAladin(topic,customUserDetails); // 알라딘에서 도서 반환 메서드 호출
        chatBotManager.deleteAllMessages(customUserDetails.getMemberId()); // Redis에 저장된 모든 대화 메시지 삭제

        return recommendations;
    }

    /* 추천 도서 장바구니에 저장 메서드 */
    @Override
    @Transactional
    @CacheEvict(value = "myCartList", key = "#customUserDetails.memberId")
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
