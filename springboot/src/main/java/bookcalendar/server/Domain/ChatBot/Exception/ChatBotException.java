package bookcalendar.server.Domain.ChatBot.Exception;

import bookcalendar.server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChatBotException extends RuntimeException {
    private final ErrorCode errorcode;
}
