package bookcalendar.server.Global.JWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.accessexpiration}")
    private long ACCESS_TOKEN_EXPIRATION_TIME;

    @Value("${jwt.refreshexpiration}")
    private long REFRESH_TOKEN_EXPIRATION_TIME;

    // TODO : 매개변수 및 내부 함수 수정
    public String generateAccessToken(String phone_number,String nickname,Long userNumber) {
        return Jwts.builder()
                .setSubject(phone_number)
                .claim("userNumber",userNumber)
                .claim("nickName", nickname)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // TODO : 매개변수 및 내부 함수 수정
    public String generateRefreshToken(String phone_number,String nickname,Long userNumber) {
        return Jwts.builder()
                .setSubject(phone_number)
                .claim("userNumber",userNumber)
                .claim("nickName", nickname)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    /**
     * 사용자 요청으로 부터 엑세스 토큰 추출
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

    // ======================= 유저 고유 번호 추출 기능 =========================

    /**
     * [1단계] HTTP 요청에서 액세스 토큰을 추출하고, JWT 내부의 userNumber 클레임을 반환한다.
     *
     * @param request HTTP 요청 객체
     * @return JWT 토큰에서 추출한 유저 고유번호 (userNumber)
     */
    public Long extractUserNumberFromRequest(HttpServletRequest request) {
        String accessToken = extractAccessToken(request);
        return extractUserNumberFromToken(accessToken);
    }

    /**
     * [2단계] JWT 토큰에서 userNumber 클레임 값을 Long 타입으로 파싱한다.
     *
     * @param token JWT 액세스 토큰
     * @return userNumber 클레임 값 (Long)
     */
    public Long extractUserNumberFromToken(String token) {
        return extractClaims_userNumber(token).get("userNumber", Long.class);
    }

    /**
     * [3단계] JWT 파서를 통해 Claims 객체(JWT 페이로드)를 추출한다.
     *
     * @param token JWT 액세스 토큰
     * @return JWT Claims 객체 (페이로드 부분)
     */
    public Claims extractClaims_userNumber(String token) {
        return Jwts.parser() // JWT 파서 객체 생성
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    // ======================= 닉네임 추출 기능 =========================

    /**
     * [1단계] HTTP 요청에서 액세스 토큰을 추출하고, JWT 내부의 nickName 클레임을 반환한다.
     * @param request
     * @return 닉네임 (String)
     */
    public String extractNickNameFromRequest(HttpServletRequest request){
        String accessToken = extractAccessToken(request);
        return extractUsername(accessToken); // [2단계]
    }

    /**
     * [2단계] JWT 토큰에서 nickName 클레임 값을 String 타입으로 파싱한다.
     * @param token
     * @return 닉네임 (String)
     */
    public String extractUsername(String token) {
        return extractClaims(token).get("nickName").toString(); // [3단계]
    }

    /**
     * [3단계] JWT 파서를 통해 Claims 객체(JWT 페이로드)를 추출한다.
     * 예외 발생 시 로깅 후 전파한다.
     * @param token
     * @return Claims 객체
     */
    public Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("x  Failed to extract claims from token: {}", token, e);
            throw e; // 필요 시 커스텀 예외로 전환 가능
        }
    }

    // ======================= 로그아웃 시 Redis Black List 등록 기능 =========================

    /**
     * 토큰 검증 메서드 (블랙리스트 체크 포함)
     * @param token 사용자의 엑세스 토큰
     * @return true & false
     */
    public boolean validateToken(String token) {

        try {
            // 블랙리스트 체크
            if (isTokenBlacklisted(token)) {
                return false;
            }
            // JWT 자체 유효성 검사
            Claims claims = extractClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("Token validation failed: {}", token, e);
            return false;
        }
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     * @param token 사용자의 엑세스 토큰
     * @return true & flase
     */
    public boolean isTokenBlacklisted(String token) {
        return redisTemplate.hasKey("blacklist:" + token);
    }

}
