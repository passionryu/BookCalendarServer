package bookcalendar.server.Domain.Member.Service;

import bookcalendar.server.Domain.Member.DTO.Request.LoginRequest;
import bookcalendar.server.Domain.Member.DTO.Request.RegisterRequest;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Exception.MemberException;
import bookcalendar.server.Domain.Member.Repository.MemberRepository;
import bookcalendar.server.global.exception.ErrorCode;
import bookcalendar.server.global.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final JwtService jwtService;

    @Qualifier("sessionRedisTemplate")
    @Autowired
    private RedisTemplate<String, String> sessionRedisTemplate;

    // ======================= 회원가입 로직 =========================

    /**
     * 회원가입 메서드
     *
     * @param registerRequest 회원가입 데이터
     * @return member 객체
     *
     * @see #nicknameExists(String)
     * @see #phoneNumberExists(String)
     */
    @Override
    public Member register(RegisterRequest registerRequest) {

        // 닉네임 중복 체크 로직
        if (nicknameExists(registerRequest.nickName())) {
            throw new MemberException(ErrorCode.ALREADY_EXIST_NICKNAME);
        }

        // 전화번호 중복 체크 로직
        if (phoneNumberExists(registerRequest.phoneNumber())) {
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
                .rank(100)
                .role("USER")
                .build();

        // Member 엔티티 DB 저장 후 반환
        return memberRepository.save(member);
    }

    /**
     * 닉네임 중복 체크 메서드
     *
     * @param nickname 회원가입 요청이 들어온 닉네임
     * @return Ture / False
     */
    private boolean nicknameExists(String nickname) {
        return memberRepository.existsByNickName(nickname);
    }

    /**
     * 전화번호 중복 체크 메서드
     *
     * @param phoneNumber 회원가입 요청이 들어온 전화번호
     * @return Ture / False
     */
    private boolean phoneNumberExists(String phoneNumber) {
        return memberRepository.existsByPhoneNumber(phoneNumber);
    }

    // ======================= 로그인 로직 =========================

    /**
     * 로그인 메서드
     *
     * @param loginRequest 로그인 요청 (nickname + password)
     * @return JWT AccessToken
     */
    @Override
    public String login(LoginRequest loginRequest) {

        // 닉네임으로 사용자 조회
        Member member = memberRepository.findByNickName(loginRequest.nickName())
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));
        log.info("member : {}", member );

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
                Duration.ofHours(1));

        // AccessToken은 클라이언트에 반환
        return accessToken;
    }

}
