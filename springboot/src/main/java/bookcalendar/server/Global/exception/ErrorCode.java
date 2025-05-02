package bookcalendar.server.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    /* Global Exceoption 0xx */
    DATABASE_ERROR("GLOBAL_ERROR_001","데이터 베이스 오류",HttpStatus.INTERNAL_SERVER_ERROR),

    /* Member Exception 6XX */
    ALREADY_EXIST_NICKNAME("MEMBER_ERROR_600", "이미 존재하는 닉네임입니다.", HttpStatus.CONFLICT),
    ALREADY_EXIST_PHONE_NUMBER("MEMBER_ERROR_601", "이미 등록된 전화번호 입니다.", HttpStatus.CONFLICT),
    USER_NOT_FOUND("MEMBER_ERROR_602", "해당 닉네임으로 조회되는 유저가 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_PASSWORD("MEMBER_ERROR_603", "비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_NOT_MATCHED("MEMBER_ERROR_604", "제출하신 리프레시 토큰이 세션에 저장된 리프레시 토큰과 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    NO_AUTH("MEMBER_ERROR_605","본 유저에게 해당 서비스를 요청할 권한이 없습니다.",HttpStatus.UNAUTHORIZED),

    /* Book Exception 7XX */
    READING_BOOK_ALREADY_EXIST("BOOK_ERROR_700","이미 도서중인 책이 있습니다. 해당 책을 완독 혹은 포기 후 다시 도서를 등록하시오",HttpStatus.CONFLICT),
    BOOK_NOT_FOUND("BOOK_ERROR_701", "해당 아이디로 등록된 도서를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    /* Review Exception 8XX */
    ALREADY_EXIST_REVIEW("REVIEW_ERROR_800", "오늘 이미 작성한 독후감이 존재합니다.", HttpStatus.CONFLICT),
    REVIEW_NOT_FOUND("REVIEW_ERROR_801", "해당 날짜에 작성된 독후감을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    /* Question Exception 9XX */
    QUESTION_NOT_FOUND("QUESTION_ERROR_900","요청하신 질문 객체가 존재하지 않습니다.",HttpStatus.NOT_FOUND),

    /* ChatBot Exception 10XX */
    FAILED_TO_PARSE("CHATBOT_ERROR_1000", "AI 응답 파싱 실패", HttpStatus.NOT_FOUND),

    /* Community Exception 11XX */
    POST_NOT_FOUND("COMMUNITY_ERROR_1100", "해당 게시글을 찾을 수 없습니다." , HttpStatus.NOT_FOUND),
    COMMENT_NOT_FOUND("COMMUNITY_ERROR_1101", "해당 댓글을 찾을 수 없습니다." , HttpStatus.NOT_FOUND);

    private final String errorCode;
    private final String message;
    private final HttpStatus httpStatus;
}
