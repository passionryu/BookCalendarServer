package bookcalendar.server.global.jwt;

import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Exception.MemberException;
import bookcalendar.server.Domain.Member.Repository.MemberRepository;
import bookcalendar.server.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Slf4j
@Service
public class JwtService {

    private final RedisTemplate<String, String> cacheRedis;
    private final RedisTemplate<String, String> sessionRedis;
    private final MemberRepository memberRepository;

    public JwtService(
            @Qualifier("cacheRedisTemplate") RedisTemplate<String, String> cacheRedis,
            @Qualifier("sessionRedisTemplate") RedisTemplate<String, String> sessionRedis,
            MemberRepository memberRepository) {
        this.cacheRedis = cacheRedis;
        this.sessionRedis = sessionRedis;
        this.memberRepository = memberRepository;
    }

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.accessexpiration}")
    private long ACCESS_TOKEN_EXPIRATION_TIME;

    @Value("${jwt.refreshexpiration}")
    private long REFRESH_TOKEN_EXPIRATION_TIME;

    // ======================= 토큰 생성 로직 =========================

    // User Meta Data : userNumber, nickName, password,phoneNumber,genre ,job,
    // birth,Role
    // JWT input data : userNumber, Role

    public String generateAccessToken(Integer userNumber,String nickName) {
        return Jwts.builder()
                .setSubject("bookcalendarUser")
                .claim("userNumber", userNumber)
                .claim("nickName", nickName)
                .claim("Role", "USER")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public String generateRefreshToken(Integer userNumber,String nickName) {
        return Jwts.builder()
                .setSubject("bookcalendarUser")
                .claim("userNumber", userNumber)
                .claim("nickName", nickName)
                .claim("Role", "USER")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // ======================= 토큰에서 멤버 객체 추출 로직 =========================

    public Member getMemberFromToken(String token) {
        Integer memberId = extractUserNumberFromToken(token);
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));
    }


    // ======================= 요청에서 엑시스 토큰 추출 로직 =========================

    /**
     * 사용자 요청으로 부터 엑세스 토큰 추출
     * 
     * @param request 사용자 요청
     * @return 엑세스 토큰 반환
     */
    public String extractAccessToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    // ======================= 요청에서 유저 고유 번호 추출 로직 =========================

    /**
     * [1단계] HTTP 요청에서 액세스 토큰을 추출하고, JWT 내부의 userNumber 클레임을 반환한다.
     *
     * @param request HTTP 요청 객체
     * @return JWT 토큰에서 추출한 유저 고유번호 (userNumber)
     */
    public Integer extractUserNumberFromRequest(HttpServletRequest request) {
        String accessToken = extractAccessToken(request);
        return extractUserNumberFromToken(accessToken);
    }

    /**
     * [2단계] JWT 토큰에서 userNumber 클레임 값을 Long 타입으로 파싱한다.
     *
     * @param token JWT 액세스 토큰
     * @return userNumber 클레임 값 (Long)
     *
     * @see #extractClaims(String) 클레임 추출 메서드
     */
    public Integer extractUserNumberFromToken(String token) {
        return extractClaims(token).get("userNumber", Integer.class);
    }

    // ======================= 요청에서 유저 nickName 추출 로직 =========================

    /**
     * [1단계] HTTP 요청에서 액세스 토큰을 추출하고, JWT 내부의 nickName 클레임을 반환한다.
     *
     * @param request HTTP 요청 객체
     * @return JWT 토큰에서 추출한 유저 nickName (String)
     */
    public String extractNickNameFromRequest(HttpServletRequest request) {
        String accessToken = extractAccessToken(request);
        return extractNickNameFromToken(accessToken);
    }

    /**
     * [2단계] JWT 토큰에서 nickName 클레임 값을 String 타입으로 파싱한다.
     *
     * @param token JWT 액세스 토큰
     * @return nickName 클레임 값 (String)
     *
     * @see #extractClaims(String) 클레임 추출 메서드
     */
    public String extractNickNameFromToken(String token) {
        return extractClaims(token).get("nickName", String.class);
    }

    // ======================= 요청에서 유저 Role 추출 로직 =========================

    /**
     * [1단계] HTTP 요청에서 액세스 토큰을 추출하고, JWT 내부의 Role 클레임을 반환한다.
     *
     * @param request HTTP 요청 객체
     * @return JWT 토큰에서 추출한 유저 Role (String)
     */
    public String extractRoleFromRequest(HttpServletRequest request) {
        String accessToken = extractAccessToken(request);
        return extractRoleFromToken(accessToken);
    }

    /**
     * [2단계] JWT 토큰에서 Role 클레임 값을 String 타입으로 파싱한다.
     *
     * @param token JWT 액세스 토큰
     * @return Role 클레임 값 (String)
     *
     * @see #extractClaims(String) 클레임 추출 메서드
     */
    public String extractRoleFromToken(String token) {
        return extractClaims(token).get("Role", String.class);
    }

    // ======================= RefreshToken 저장 및 refresh token rotation 로직  =========================

    /**
     * 리프레쉬 토큰 세션 레디스에 저장 메서드
     *
     * @param memberId 로그인하는 유저의 고유 번호
     * @param refreshToken 로그인 한 후 반환된 리프레쉬 토큰(세션에 저장)
     */
    public void saveRefreshTokenToSessionRedis(Integer memberId,String refreshToken) {

        log.info("length : {}",refreshToken.getBytes().length);
        String key = "refresh_token:" + memberId;
        sessionRedis.opsForValue().set(key, refreshToken, REFRESH_TOKEN_EXPIRATION_TIME);

    }

    /* 기간 만료된 AccessToken 입력 시 새로운 accesstoken, refreshToken반환 (RTR) */


    // ======================= Logout 후 Black List에 JWT 토큰 등록 로직

    /**
     * 토큰 검증 메서드 (블랙리스트 체크 포함)
     * 
     * @param token 사용자의 엑세스 토큰
     * @return true & false
     *
     * @see #isTokenBlacklisted(String) 블랙리스트 확인 메서드
     * @see #extractClaims(String) 클레임 추출 메서드
     */
    public boolean validateToken(String token) {

        try {
            // [1단계] 블랙리스트 확인 → 아래 isTokenBlacklisted() 참조
            if (isTokenBlacklisted(token)) {
                return false;
            }
            // [2단계] JWT 자체 유효성 검사
            Claims claims = extractClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("Token validation failed: {}", token, e);
            return false;
        }
    }

    /**
     * Redis에 저장된 블랙리스트에 토큰이 있는지 확인
     *
     * @param token 사용자의 액세스 토큰
     * @return 블랙리스트에 있으면 true, 아니면 false
     */
    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(sessionRedis.hasKey("blacklist:" + token));
    }

    // ======================= Util Code =========================

    /**
     * 클레임 추출 메서드
     * 
     * @param token
     * @return
     *
     */
    public Claims extractClaims(String token) {
        return Jwts.parser() // JWT 파서 객체 생성
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

//    public Claims extractClaims(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }

}
