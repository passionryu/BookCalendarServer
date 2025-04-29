package bookcalendar.server.Domain.Member.Service;

import bookcalendar.server.Domain.Member.DTO.Request.LoginRequest;
import bookcalendar.server.Domain.Member.DTO.Request.TokenRequest;
import bookcalendar.server.Domain.Member.DTO.Request.RegisterRequest;
import bookcalendar.server.Domain.Member.DTO.Response.RankResponse;
import bookcalendar.server.Domain.Member.DTO.Response.TokenResponse;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Exception.MemberException;
import bookcalendar.server.Domain.Member.Repository.MemberRepository;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.exception.ErrorCode;
import bookcalendar.server.global.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    @Qualifier("sessionRedisTemplate")
    @Autowired
    private RedisTemplate<String, String> sessionRedisTemplate;

    private final BCryptPasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final JwtService jwtService;

    @Value("${jwt.refreshexpiration}")
    private long REFRESH_TOKEN_EXPIRATION_TIME;


    // ======================= 회원가입 로직 =========================

    /**
     * 회원가입 메서드
     *
     * @param registerRequest 회원가입 데이터
     * @return member 객체
     */
    @Override
    public Member register(RegisterRequest registerRequest) {

        // 닉네임 중복 체크 로직
        if (memberRepository.existsByNickName(registerRequest.nickName())) {
            throw new MemberException(ErrorCode.ALREADY_EXIST_NICKNAME);
        }

        // 전화번호 중복 체크 로직
        if (memberRepository.existsByPhoneNumber(registerRequest.phoneNumber())) {
            throw new MemberException(ErrorCode.ALREADY_EXIST_PHONE_NUMBER);
        }

        // 비밀번호 해싱
        String encodedPassword = passwordEncoder.encode(registerRequest.password());

        // Member 엔티티 생성
        Member member = Member.builder()
                .nickName(registerRequest.nickName())
                .password(encodedPassword)
                .birth(registerRequest.birth())
                .phoneNumber(registerRequest.phoneNumber())
                .genre(registerRequest.genre())
                .job(registerRequest.job())
                .completion(0)
                .reviewCount(0)
                .rank(100)
                .role("USER")
                .build();

        // Member 엔티티 DB 저장 후 반환
        return memberRepository.save(member);
    }

    // ======================= 로그인 로직 =========================

    /**
     * 로그인 메서드
     *
     * @param loginRequest 로그인 요청 (nickname + password)
     * @return JWT AccessToken
     */
    @Override
    public TokenResponse login(LoginRequest loginRequest) {

        // 닉네임으로 사용자 조회
        Member member = memberRepository.findByNickName(loginRequest.nickName())
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));

        // 비밀번호 검증
        if (!passwordEncoder.matches(loginRequest.password(), member.getPassword())) {
            throw new MemberException(ErrorCode.INVALID_PASSWORD);
        }

        // 모든 조건이 만족되면, JWT 생성
        String accessToken = jwtService.generateAccessToken(member.getMemberId(),member.getNickName());
        String refreshToken = jwtService.generateRefreshToken(member.getMemberId(),member.getNickName());

        // redis-session 저장소에 refreshToken 저장
        sessionRedisTemplate.opsForValue().set(
                String.valueOf(member.getMemberId()),
                refreshToken,
                Duration.ofMillis(REFRESH_TOKEN_EXPIRATION_TIME));

        // AccessToken은 클라이언트에 반환
        return new TokenResponse(accessToken,refreshToken);
    }

    // ======================= 토큰 최신화 로직 =========================

    /**
     * 토큰 최신화 메서드
     *
     * @param refreshRequest 유저가 전송한 리프레시 토큰
     * @return JWT 토큰 DTO
     */
    @Override
    public TokenResponse refreshToken(TokenRequest refreshRequest) {

        // 리프레시 토큰 유효성 검사

        // 토큰에서 유저 고유 번호 추출
        Integer memberId = jwtService.extractUserNumberFromToken(refreshRequest.accessToken());

        // 전송받은 리프레시 토큰 추출
        String oldRefreshToken = refreshRequest.refreshToken();

        // 전송받은 리프레시 토큰 - redis session에 있는 리프레시 토큰 비교
        String redisRefreshToken = sessionRedisTemplate.opsForValue().get(memberId.toString());
        if (redisRefreshToken == null || !redisRefreshToken.equals(oldRefreshToken)) {
            throw new MemberException(ErrorCode.REFRESH_TOKEN_NOT_MATCHED);
        }

        // 새 토큰 발급
        String newAccessToken = jwtService.generateAccessToken(memberId, jwtService.extractNickNameFromToken(refreshRequest.accessToken()));
        String newRefreshToken = jwtService.generateRefreshToken(memberId, jwtService.extractNickNameFromToken(refreshRequest.accessToken()));

        // 기존 리프레시 토큰 제거
        sessionRedisTemplate.delete(String.valueOf(memberId));

        // 새 리프레시 토큰 저장
        sessionRedisTemplate.opsForValue().set(
                String.valueOf(memberId),
                newRefreshToken,
                Duration.ofMillis(REFRESH_TOKEN_EXPIRATION_TIME));

        return new TokenResponse(newAccessToken,newRefreshToken);
    }

}
