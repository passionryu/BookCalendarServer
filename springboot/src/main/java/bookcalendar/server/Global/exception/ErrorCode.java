package bookcalendar.server.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    /* Member Exception 6XX */
    ALREADY_EXIST_NICKNAME("MEMBER_600", "이미 존재하는 닉네임입니다.", HttpStatus.CONFLICT),
    ALREADY_EXIST_PHONE_NUMBER("MEMBER_601", "이미 등록된 전화번호 입니다.", HttpStatus.CONFLICT),
    USER_NOT_FOUND("MEMBER_602", "해당 닉네임으로 조회되는 유저가 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_PASSWORD("MEMBER_603", "비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_NOT_MATCHED("MEMBER_6004", "제출하신 리프레시 토큰이 세션에 저장된 리프레시 토큰과 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);

    private final String errorCode;
    private final String message;
    private final HttpStatus httpStatus;
}
