package bookcalendar.server.Domain.MockAiModel.Emotion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmotionMockModel {

    private final ChatClient chatClient;

    public String numberOneQuestion(String contents){

        String contensPrompt = """
        다음 글은 사용자의 독후감입니다.
        이 독후감을 읽고 사용자의 감정을 (기쁨, 당황, 분노, 불안, 슬픔) 중 하나로만 분류해주세요.
        **다른 말은 하지 말고**, 감정 단어 하나만 출력하세요. 예: 기쁨
        독후감: """ + contents;

        String emotion = chatClient.call(contensPrompt).trim();
        log.info("emotion : {}", emotion);

        return emotion;
    }

}
