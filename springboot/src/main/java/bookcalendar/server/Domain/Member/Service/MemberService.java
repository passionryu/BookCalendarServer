package bookcalendar.server.Domain.Member.Service;

import bookcalendar.server.Domain.Member.DTO.Request.LoginRequest;
import bookcalendar.server.Domain.Member.DTO.Request.TokenRequest;
import bookcalendar.server.Domain.Member.DTO.Request.RegisterRequest;
import bookcalendar.server.Domain.Member.DTO.Response.RankResponse;
import bookcalendar.server.Domain.Member.DTO.Response.TokenResponse;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.global.Security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;

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
     * @return 엑세스 토큰, 리프레시 토큰
     */
    TokenResponse login(LoginRequest loginRequest);

    /**
     * 로그아웃 인터페이스
     *
     * @param httpServletRequest
     * @return
     */
    void logout(HttpServletRequest httpServletRequest);

    /**
     * 토큰 최신화 인터페이스
     *
     * @param refreshRequest
     * @return 새로운 토큰 객체
     */
    TokenResponse refreshToken(TokenRequest refreshRequest);


}
