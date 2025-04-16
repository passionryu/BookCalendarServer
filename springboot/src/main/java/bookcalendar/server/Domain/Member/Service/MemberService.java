package bookcalendar.server.Domain.Member.Service;

import bookcalendar.server.Domain.Member.DTO.Request.LoginRequest;
import bookcalendar.server.Domain.Member.DTO.Request.TokenRequest;
import bookcalendar.server.Domain.Member.DTO.Request.RegisterRequest;
import bookcalendar.server.Domain.Member.DTO.Response.RankResponse;
import bookcalendar.server.Domain.Member.DTO.Response.TokenResponse;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.global.Security.CustomUserDetails;

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
    TokenResponse login(LoginRequest loginRequest);

    /**
     * 토큰 최신화 인터페이스
     *
     * @param refreshRequest
     * @return
     */
    TokenResponse refreshToken(TokenRequest refreshRequest);

    /**
     * 유저 메달 및 랭킹 반환 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return
     */
    RankResponse getRank(CustomUserDetails customUserDetails);
}
