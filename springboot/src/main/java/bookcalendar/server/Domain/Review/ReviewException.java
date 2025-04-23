package bookcalendar.server.Domain.Review;


import bookcalendar.server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReviewException extends RuntimeException{
    private final ErrorCode errorcode;
}
