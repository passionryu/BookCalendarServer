package bookcalendar.server.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    /* Global Exceoption 0xx */
    DATABASE_ERROR("GLOBAL001","데이터 베이스 오류",HttpStatus.INTERNAL_SERVER_ERROR),

    /* Member Exception 6XX */
    ALREADY_EXIST_NICKNAME("MEMBER_600", "이미 존재하는 닉네임입니다.", HttpStatus.CONFLICT),
    ALREADY_EXIST_PHONE_NUMBER("MEMBER_601", "이미 등록된 전화번호 입니다.", HttpStatus.CONFLICT),
    USER_NOT_FOUND("MEMBER_602", "해당 닉네임으로 조회되는 유저가 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_PASSWORD("MEMBER_603", "비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_NOT_MATCHED("MEMBER_604", "제출하신 리프레시 토큰이 세션에 저장된 리프레시 토큰과 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),

    /* Book Exception 7XX */
    READING_BOOK_ALREADY_EXIST("BOOK_700","이미 도서중인 책이 있습니다. 해당 책을 완독 혹은 포기 후 다시 도서를 등록하시오",HttpStatus.CONFLICT),
    BOOK_NOT_FOUND("BOOK_701", "해당 아이디로 등록된 도서를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    /* Review Exception 8XX */
    ALREADY_EXIST_REVIEW("REVIEW_800", "오늘 이미 작성한 독후감이 존재합니다.", HttpStatus.CONFLICT),
    REVIEW_NOT_FOUND("REVIEW_801", "해당 날짜에 작성된 독후감을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    /* Question Exception 7XX */
    QUESTION_NOT_FOUND("QUESTION_700","요청하신 질문 객체가 존재하지 않습니다.",HttpStatus.NOT_FOUND),;


    private final String errorCode;
    private final String message;
    private final HttpStatus httpStatus;
}
