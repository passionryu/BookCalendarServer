package bookcalendar.server.Domain.ChatBot.Service;

import bookcalendar.server.Domain.Book.DTO.Response.CompleteResponse;
import bookcalendar.server.Domain.Book.Entity.Book;
import bookcalendar.server.Domain.Book.Exception.BookException;
import bookcalendar.server.Domain.ChatBot.DTO.Request.ChatRequest;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Exception.MemberException;
import bookcalendar.server.Domain.Member.Repository.MemberRepository;
import bookcalendar.server.Domain.Review.Entity.Review;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService{

    @Qualifier("sessionRedisTemplate")
    @Autowired
    private RedisTemplate<String, String> sessionRedisTemplate;

    private final ChatClient chatClient;
    private final MemberRepository memberRepository;

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

        /* Redis DB에서 기존의 대화내용이 있으면 반환 */
        String previousMessage  = getPreviousMessage(customUserDetails.getMemberId());

        /* 사용자 메시지 + AI 프롬프트 메시징 */
        String promptMessage = makePromptMessage(chatRequest.chatMessage(), previousMessage);

        /* AI 상담사 챗봇 답변 반환 */
        String aiResponse = chatClient.call(promptMessage);

        /* 문자열 정제 */
        String cleanAiResponse = aiResponse.replaceAll("(?i)^counselor:\\s*", "");

        /* 유저 메시지,챗봇 답변 Redis에 업로드 */
        saveMessageInRedis(customUserDetails.getMemberId(),chatRequest.chatMessage(),cleanAiResponse);

        /* 최종적으로 AI 챗봇의 답변을 반환 */
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

        // 현재 멤버 객체 반환
        Member member = memberRepository.findByMemberId(customUserDetails.getMemberId())
                .orElseThrow(()-> new MemberException(ErrorCode.USER_NOT_FOUND) );

        // 현재 유저의 나의 계산
        int age = Period.between(member.getBirth(), LocalDate.now()).getYears();

        // 모든 메시지 반환
        String everyMessages = getAllMessage(customUserDetails.getMemberId());
        log.info("everyMessages : {}",everyMessages);

        // AI 프롬프트 (반드시 JSON 배열로 반환하도록 지시)
        String aiPromptMessage = String.format(
                """
                너는 이 도서 추천 서비스의 유능한 AI 도서 추천 사서이다.
                다음은 너가 사용자와의 챗봇 서비스에서 나눈 대화 내용이다
                다음은 내용들을 참고하여 사용자에게 도서 5권을 추천해w:
    
                - 너(AI 도서 추천 사서 와 사용자와의 지난 모든 대화): "%s"
    
                아래 JSON 형식으로 꼭 반환해줘:
    
                [
                  {
                    "bookName": "책 제목",
                    "author": "저자 이름",
                    "reason": "이 도서를 추천하는 이유2~3줄"
                  },
                  ...
                ]
                """,
                everyMessages
        );

        String aiResponse = chatClient.call(aiPromptMessage);

        // 🛠 JSON 문자열을 CompleteResponse 리스트로 파싱
        ObjectMapper objectMapper = new ObjectMapper();
        List<CompleteResponse> recommendations;

        try {
            recommendations = objectMapper.readValue(
                    aiResponse,
                    new TypeReference<>() {}
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("AI 응답 파싱 실패: " + e.getMessage(), e);
        }

        return recommendations;

    }

    // ======================= Chatbot Helper Code =========================

    /**
     * 이전 메시지 반환 메서드
     *
     * @param userNumber
     * @return
     */
    private String getPreviousMessage(Integer userNumber){

        /* 사용자 고유번호를 이용한 redis key 생성*/
        String key = userNumber + ":chat";

        /* 리스트에서 마지막 6개의 메시지를 반환 */
        List<String> messages = sessionRedisTemplate.opsForList().range(key, 0, 6);

        String everyMessage = String.join(" ", messages);
        log.info("every Messgae : {}",everyMessage);

        /* 여러개의 메시지를 하나의 문자열로 결합하여 반환 */
        return everyMessage;
    }

    /**
     * 메시지 프롬프팅 메서드
     *
     * @param message 사용자의 이번 메시지
     * @param previousMessage 사용자의 기존 메시지
     * @return 프롬프트 메시지
     */
    public String makePromptMessage(String message,String previousMessage){

        return "당신의 역할은 AI 도서 추천 전문가이다. 당신이 이전에 사용자와 나눈 내용은 다음과 같다"
                + previousMessage
                + "이를 참고하여 사용자의 다음 메시지를 보고 적절히 답변하라."
                + message;
    }

    /**
     * Redis에서 사용자와 챗봇의 메시지 저장
     *
     * @param id 사용자 고유 번호
     * @param userMessage 사용자 메시지
     * @param aiResponse AI 챗봇의 답변
     */
    public void saveMessageInRedis(Integer id,String userMessage ,String aiResponse){
        // Redis에 사용자와 챗봇의 메시지 저장
        saveMessage(id, userMessage, "user");
        saveMessage(id, aiResponse, "counselor");
    }

    /**
     * Redis에 메시지 저장 형식 정의 메서드
     *
     * @param id 사용자 고유 번호
     * @param message 사용자 메시지
     * @param sender 전송자
     */
    public void saveMessage(Integer id, String message, String sender) {
        String key = id + ":chat" ;
        String taggedMessage = sender + ": " + message;
        sessionRedisTemplate.opsForList().rightPush(key, taggedMessage);
    }

    /**
     * 레디스에서 모든 메시지 반환 메서드
     *
     * @param userNumber 사용자 고유 번호
     * @return 모든 채팅 메시지
     */
    public String getAllMessage(Integer userNumber){

        String key = userNumber + ":chat";
        List<String> messages = sessionRedisTemplate.opsForList().range(key, 0, -1);

        /* 메시지를 하나의 문자열로 결합하여 반환 */
        return String.join(" ", messages);
    }

    /**
     * AI 추천 메서드에서의 프롬프트 메시지
     *
     * @param everyMessages 유저와 챗봇의 모든 메시지
     * @return 프롬프트 메시지
     */
    public String resultPromptMessage(String everyMessages){

        return
                "다음은 유저와 너가 대화한 채팅 내용이다."

                        + everyMessages;
    }

    /**
     * 레디스에서 모든 메시지 삭제
     *
     * @param userNumber 사용자 고유 번호
     */
    public void deleteAllMessages(Integer userNumber) {
        String key = userNumber + ":chat";
        sessionRedisTemplate.delete(key);
        log.info("Redis에서 메시지 삭제 완료: {}", key);
    }
}
