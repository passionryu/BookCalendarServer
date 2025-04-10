package bookcalendar.server.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    /* Member Exception 6XX */
    ALREADY_EXIST_NICKNAME("MEMBER_600", "이미 존재하는 닉네임입니다.", HttpStatus.CONFLICT),
    ALREADY_EXIST_PHONE_NUMBER("MEMBER_601", "이미 등록된 전화번호 입니다.", HttpStatus.CONFLICT);


    private final String errorCode;
    private final String message;
    private final HttpStatus httpStatus;
}
