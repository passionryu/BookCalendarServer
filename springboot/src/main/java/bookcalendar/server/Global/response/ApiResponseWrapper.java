package bookcalendar.server.global.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponseWrapper<T> {

    private T data; // 반환값 = 제네릭
    private String message; // 응답 메시지
    private String errorCode;  // 에러 메시지 필드 추가

    public ApiResponseWrapper(T data, String message) {

        this.data = data;
        this.message = message;
    }

    /*public ApiResponseWrapper(T data, String errorMessage, String errorCode) {
        this.data=data
        this.errorCode = errorCode;
        this.message = errorMessage;
    }*/

}