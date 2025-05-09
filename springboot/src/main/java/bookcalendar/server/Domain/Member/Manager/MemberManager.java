package bookcalendar.server.Domain.Member.Manager;

import bookcalendar.server.Domain.Member.DTO.Response.TokenResponse;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Exception.MemberException;
import bookcalendar.server.Domain.Member.Repository.MemberRepository;
import bookcalendar.server.global.exception.ErrorCode;
import bookcalendar.server.global.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberManager {

    @Qualifier("sessionRedisTemplate")
    @Autowired
    private RedisTemplate<String, String> sessionRedisTemplate;

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // ======================= 회원가입 영역=========================

    /* 닉네임 중복 체크 메서드 */
    public void checkNickNameDuplication(String nickName) {
        if (memberRepository.existsByNickName(nickName))
            throw new MemberException(ErrorCode.ALREADY_EXIST_NICKNAME);
    }

    /* 전화번호 중복 체크 메서드 */
    public void checkPhoneNumberDuplication(String phoneNumber) {
        if (memberRepository.existsByPhoneNumber(phoneNumber))
            throw new MemberException(ErrorCode.ALREADY_EXIST_PHONE_NUMBER);
    }

    // ======================= 로그인 영역 =========================

    /* 닉네임으로 사용자 조회 메서드 */
    public Member getMemberByNickName(String nickName) {
        return memberRepository.findByNickName(nickName)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));
    }

    /* 입력한 비밀번호 검증 메서드 */
    public void checkPasswordValidation(String requestPassword,String savedPassword ) {
        if (!passwordEncoder.matches(requestPassword, savedPassword))
            throw new MemberException(ErrorCode.INVALID_PASSWORD);
    }

    /* JWT 토큰 생성 메서드 */
    public TokenResponse generateTokensForMember(Member member) {
        String accessToken = jwtService.generateAccessToken(member.getMemberId(), member.getNickName());
        String refreshToken = jwtService.generateRefreshToken(member.getMemberId(), member.getNickName());

        return new TokenResponse(accessToken, refreshToken);
    }

    /* redis-session 저장소에 refreshToken 저장 메서드 */
    public void saveRefreshTokenToRedis(Member member, String refreshToken, long expirationTime) {
        sessionRedisTemplate.opsForValue().set(
                String.valueOf(member.getMemberId()), refreshToken, Duration.ofMillis(expirationTime)
        );
    }

    // ======================= 리프레시 토큰 로테이션 영역 =========================


    /* 리프레시 토큰 재발급 */
    public TokenResponse rotateRefreshToken(String accessToken, String oldRefreshToken, long expirationTime) {
        Integer memberId = jwtService.extractUserNumberFromToken(accessToken);
        String nickName = jwtService.extractNickNameFromToken(accessToken);

        validateRefreshToken(memberId, oldRefreshToken); // Redis 저장된 토큰과 비교

        deleteOldRefreshToken(memberId); // 기존 토큰 제거

        String newAccessToken = jwtService.generateAccessToken(memberId, nickName);
        String newRefreshToken = jwtService.generateRefreshToken(memberId, nickName);

        // 새 리프레시 토큰 저장
        sessionRedisTemplate.opsForValue().set(
                String.valueOf(memberId),
                newRefreshToken,
                Duration.ofMillis(expirationTime));

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    /* 리프레시 토큰 일치 여부 확인 */
    public void validateRefreshToken(Integer memberId, String oldRefreshToken) {
        String redisRefreshToken = sessionRedisTemplate.opsForValue().get(memberId.toString());
        if (redisRefreshToken == null || !redisRefreshToken.equals(oldRefreshToken))
            throw new MemberException(ErrorCode.REFRESH_TOKEN_NOT_MATCHED);

    }

    /* 기존 리프레시 토큰 삭제 */
    public void deleteOldRefreshToken(Integer memberId) {
        sessionRedisTemplate.delete(String.valueOf(memberId));
    }

}
