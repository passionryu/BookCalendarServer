package bookcalendar.server.Domain.Member.Service;

import bookcalendar.server.Domain.Member.DTO.Request.LoginRequest;
import bookcalendar.server.Domain.Member.DTO.Request.RegisterRequest;
import bookcalendar.server.Domain.Member.DTO.Request.TokenRequest;
import bookcalendar.server.Domain.Member.DTO.Response.TokenResponse;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Manager.MemberManager;
import bookcalendar.server.Domain.Member.Repository.MemberRepository;
import bookcalendar.server.global.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    @Value("${jwt.refreshexpiration}")
    private long REFRESH_TOKEN_EXPIRATION_TIME;

    private final BCryptPasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final MemberManager memberManager;
    private final JwtService jwtService;

    /* 회원가입 메서드 */
    @Override
    @Transactional
    public Member register(RegisterRequest registerRequest) {

        memberManager.checkNickNameDuplication(registerRequest.nickName()); // 닉네임 중복 체크
        memberManager.checkPhoneNumberDuplication(registerRequest.phoneNumber()); // 전화번호 중복 체크 로직

        String encodedPassword = passwordEncoder.encode(registerRequest.password()); // 비밀번호 해싱
        Member member = Member.createMember(registerRequest,encodedPassword); // 입력 받은 정보로 멤버 엔티티 생성
        return memberRepository.save(member);
    }

    /* 로그인 메서드 */
    @Override
    public TokenResponse login(LoginRequest loginRequest) {

        Member member = memberManager.getMemberByNickName(loginRequest.nickName()); // 닉네임으로 사용자 조회
        memberManager.checkPasswordValidation(loginRequest.password(), member.getPassword()); // 비밀번호 검증

        /* 위 두가지 조건이 만족되면 */
        TokenResponse tokens = memberManager.generateTokensForMember(member); // JWT 토큰 생성
        memberManager.saveRefreshTokenToRedis(member, tokens.refreshToken(), REFRESH_TOKEN_EXPIRATION_TIME); // redis-session에 refreshToken 저장
        return tokens;
    }

    /* 로그아웃 메서드 */
    @Override
    public void logout(HttpServletRequest httpServletRequest) {

        String accessToken = jwtService.extractAccessToken(httpServletRequest); // 유저의 요청으로 부터 엑세스 토큰 추출
        jwtService.addTokenToBlacklist(accessToken); // Session-Redis의 블랙리스트에 현재 유저의 엑세스 토큰 업로드
    }

    /* 리프레시 토큰 로테이션 메서드 */
    @Override
    public TokenResponse refreshToken(TokenRequest refreshRequest) {
        return memberManager.rotateRefreshToken(
                refreshRequest.accessToken(), // 새로 발급받은 엑세스 토큰
                refreshRequest.refreshToken(), // 새로 발급받은 리프레시 토큰
                REFRESH_TOKEN_EXPIRATION_TIME //리프레시 토큰 만료 시간
        );
    }
}
