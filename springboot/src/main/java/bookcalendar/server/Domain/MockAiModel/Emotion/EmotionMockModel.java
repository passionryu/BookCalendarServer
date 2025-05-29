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

        String contentsPrompt = """
                당신은 감정을 정밀하게 분류하는 전문가입니다.

                다음 글은 사용자의 독후감입니다.
                이 독후감을 읽고 사용자의 주된 감정을 아래 목록 중 **가장 잘 맞는 하나의 감정 단어로만** 분류해주세요.

                가능한 감정 목록:
                기쁨, 슬픔, 분노, 불안, 당황, 감동, 외로움, 후회, 평온, 혼란, 설렘, 무기력

                **다른 말은 절대 하지 말고**, 반드시 감정 단어 하나만 출력하세요. 예: 기쁨, 슬픔, 설렘

                독후감:
                """ + contents;


        String emotion = chatClient.call(contentsPrompt).trim();
        log.info("emotion : {}", emotion);

        return emotion;
    }

}
