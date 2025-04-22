package bookcalendar.server.Domain.Question.Exception;


import bookcalendar.server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class QuestionException extends RuntimeException{
    private final ErrorCode errorcode;
}
