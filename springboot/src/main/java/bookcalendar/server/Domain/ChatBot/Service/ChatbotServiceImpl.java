package bookcalendar.server.Domain.ChatBot.Service;

import bookcalendar.server.Domain.Book.DTO.Response.CompleteResponse;
import bookcalendar.server.Domain.ChatBot.Component.RedisManager;
import bookcalendar.server.Domain.ChatBot.DTO.Request.ChatRequest;
import bookcalendar.server.Domain.ChatBot.Static.RedisHelper;
import bookcalendar.server.global.Security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService{

    private final ChatClient chatClient;
    private final RedisManager redisManager;

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

}
