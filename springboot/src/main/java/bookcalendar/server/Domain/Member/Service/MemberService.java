package bookcalendar.server.Domain.Member.Service;

import bookcalendar.server.Domain.Member.DTO.Request.RegisterRequest;
import bookcalendar.server.Domain.Member.Entity.Member;

public interface MemberService {

    /**
     * 회원가입 기능
     *
     * @param registerRequest 회원가입 데이터
     * @return member 객체
     */
    Member register(RegisterRequest registerRequest);
}
