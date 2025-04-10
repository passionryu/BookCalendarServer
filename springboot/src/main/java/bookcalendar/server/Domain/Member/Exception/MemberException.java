package bookcalendar.server.Domain.Member.Exception;

import bookcalendar.server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MemberException extends RuntimeException {
    private final ErrorCode errorcode;
}
