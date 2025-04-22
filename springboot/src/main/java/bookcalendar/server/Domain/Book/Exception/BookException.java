package bookcalendar.server.Domain.Book.Exception;

import bookcalendar.server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BookException extends RuntimeException{
    private final ErrorCode errorcode;
}
