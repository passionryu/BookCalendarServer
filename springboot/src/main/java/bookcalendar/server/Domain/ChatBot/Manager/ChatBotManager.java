package bookcalendar.server.Domain.ChatBot.Manager;

import bookcalendar.server.Domain.Book.DTO.Response.CompleteResponse;
import bookcalendar.server.Domain.ChatBot.Helper.ChatBotHelper;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Exception.MemberException;
import bookcalendar.server.Domain.Member.Repository.MemberRepository;
import bookcalendar.server.global.BookOpenApi.Aladin.AladinService;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatBotManager {

    @Qualifier("sessionRedisTemplate")
    @Autowired
    private RedisTemplate<String, String> sessionRedisTemplate;
    @Autowired
    private ChatClient chatClient;
    private final MemberRepository memberRepository;
    private final AladinService aladinService;

    /* 도서 주제 블랙리스트 */
    private static final Set<String> INVALID_TOPICS = Set.of("책", "도서", "서적", "추천", "중고", "알라딘",
            "포장팩", "가방", "쇼핑", "상품", "판매");

    // ======================= Util 영역 =========================

    /* 현재 멤버 객체 반환 */
    public Member getMember(CustomUserDetails customUserDetails) {

        return memberRepository.findByMemberId(customUserDetails.getMemberId())
                .orElseThrow(()-> new MemberException(ErrorCode.USER_NOT_FOUND) );
    }

    // ======================= 챗봇 채팅 영역 =========================

    /**
     * 이전 메시지 반환 메서드
     *
     * @param userNumber 유저의 고유 번호
     * @return 이전 메시지 반환
     */
    public String getPreviousMessage(Integer userNumber){

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

    // ======================= 도서 추천 영역 =========================

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
     * 레디스에서 모든 메시지 삭제
     *
     * @param userNumber 사용자 고유 번호
     */
    public void deleteAllMessages(Integer userNumber) {
        String key = userNumber + ":chat";
        sessionRedisTemplate.delete(key);
        log.info("Redis에서 메시지 삭제 완료: {}", key);
    }

    /**
     * 채팅에서 주제 추출 메서드
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 주제 리스트
     */
    public String getTopicsFromMessages(CustomUserDetails customUserDetails) {

        String everyMessages = getAllMessage(customUserDetails.getMemberId()); // Redis 메모리에서 모든 메시지 반환
        String topic = getTopic(everyMessages); // 1개의 메인 주제 추출 (Gpt프롬프팅, Gpt 호출, 파싱)

        /* 주제를 추출하고 블랙리스트에 있는 값이 아니면 주제 반환 */
        if (topic == null || INVALID_TOPICS.contains(topic)) {
            /* 방어 코드 1 : 만약 메인 주제를 추출하지 못했으면 혹은 블랙리스트를 추출했으면 한번더 추출한다. */
            log.info("주제 추출 실패, 방어코드 작동 - 주제 추출 재시도");
            topic = getTopic(everyMessages);
            log.info("주제 추출 재시도 결과 : {}", topic);
            if (topic != null && !INVALID_TOPICS.contains(topic)) {
                /* 방어 코드 2 : 만약 메인 주제를 추출하지 못했으면 유저의 직업을 주제로 잡는다. */
                Member member = getMember(customUserDetails);
                topic = member.getJob();
                log.info("1차 주제 추출, 2차 주제 추출 실패 - 유저의 직업으로 도서 검색 - 유저의 직업 :{}",topic);
            }
        }
        return topic;
    }

    /* 알라딘에서 5권의 도서를 추출 */
    public List<CompleteResponse> getBookFromAladin(String topic, CustomUserDetails customUserDetails){

        List<CompleteResponse> recommendations = new ArrayList<>();

        // 주제로 Aladin에서 책 5권 반환
        List<Optional<CompleteResponse>> topic1Books = aladinService.searchTopBooksByTopic(topic, 5);
        topic1Books.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(recommendations::add);

        /* 만약 해당 주제로 알라딘에 충분한 도서가 없으면? */
        int remaining = 5 - recommendations.size();
        if (remaining > 0) {
            /* 사용자의 직업 추출 */
            Member member = getMember(customUserDetails);
            String job = member.getJob();
            log.info("주어진 주제로 5권의 도서 정상 반환 실패 - 유저의 직업인 {}을 키워드로로 남은 도서 {}권 반환", job,remaining );

            /* 방어 코드 - 부족한 권 수만큼 유저 직업 기반으로 보충 */
            List<Optional<CompleteResponse>> additionalBooks = aladinService.searchTopBooksByTopic(job, remaining);
            additionalBooks.stream()
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(recommendations::add);
        }else{
            log.info("주어진 주제로 5권의 도서 정상 반환");
        }

        log.info("도서 반환 결과 : {}", recommendations);
        return recommendations;
    }

    /* 주제를 통한 도서 반환 */
    public String getTopic(String everyMessages) {

        /* Helper 클래스에서 Gpt 프롬프팅 */
        String promptedMessage = ChatBotHelper.buildPrompt_getTopic(everyMessages);

        /* Gpt 호출 */
        String topicsResponseByGpt = chatClient.call(promptedMessage);

        /* 데이터 파싱 */
        String topic = ChatBotHelper.parseTopic(topicsResponseByGpt);

        /* 로그 출력 */
        log.info("추출한 주제 : {}", topic);

        return topic;
    }
}
