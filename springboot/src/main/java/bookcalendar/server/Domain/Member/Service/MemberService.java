package bookcalendar.server.Domain.Member.Service;

import bookcalendar.server.Domain.Member.DTO.Request.LoginRequest;
import bookcalendar.server.Domain.Member.DTO.Request.RegisterRequest;
import bookcalendar.server.Domain.Member.Entity.Member;

public interface MemberService {

    /**
     * 회원가입 인터페이스
     *
     * @param registerRequest 회원가입 데이터
     * @return member 객체
     */
    Member register(RegisterRequest registerRequest);

    /**
     * 로그인 인터페이스
     *
     * @param loginRequest
     * @return
     */
    String login(LoginRequest loginRequest);
}
