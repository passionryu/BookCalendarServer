package bookcalendar.server.global.exception;

import bookcalendar.server.Domain.Member.Exception.MemberException;
import bookcalendar.server.global.response.ApiResponseWrapper;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Member Domain Global Exception Method
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(MemberException.class)
    public ResponseEntity<ApiResponseWrapper<String>> memberExceptions(MemberException ex) {
        ApiResponseWrapper<String> response = new ApiResponseWrapper<>(ex.getErrorcode().getErrorCode(),ex.getErrorcode().getMessage());
        return new ResponseEntity<>(response, ex.getErrorcode().getHttpStatus());
    }





    // ======================================================================================================
    /**
     * @Valid오류 예외처리 메서드
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseWrapper<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        ApiResponseWrapper<List<String>> response = new ApiResponseWrapper<>(errors, String.valueOf(HttpStatus.BAD_REQUEST.value()));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


}
