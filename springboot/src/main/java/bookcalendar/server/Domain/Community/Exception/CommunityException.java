package bookcalendar.server.Domain.Community.Exception;

import bookcalendar.server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CommunityException extends RuntimeException {
    private final ErrorCode errorcode;
}