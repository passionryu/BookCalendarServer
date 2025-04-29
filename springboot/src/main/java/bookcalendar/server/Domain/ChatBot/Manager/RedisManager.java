package bookcalendar.server.Domain.ChatBot.Manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RedisManager {

    @Qualifier("sessionRedisTemplate")
    @Autowired
    private RedisTemplate<String, String> sessionRedisTemplate;

    /**
     * 이전 메시지 반환 메서드
     *
     * @param userNumber
     * @return
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
}
