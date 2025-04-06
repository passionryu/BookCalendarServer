package bookcalendar.server.global.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    // ======================= 불필요 API 패스 로직 =========================

    /**
     * 권한 불필요 API 들을 넘기는 메서드
     * 
     * @param request 사용자 요청
     * @return ture/false
     */
    private boolean isPublicApi(HttpServletRequest request) {
        String path = request.getRequestURI();
        return PUBLIC_APIS.stream().anyMatch(path::startsWith);
    }

    private static final List<String> PUBLIC_APIS = List.of(
            "/api/v1/member/register",
            "/swagger-ui/**",
            "/swagger-ui/index.html",
            "/v3/api-docs/**",
            "/webjars/");

    // ======================= Filter Chain 로직 =========================

    /**
     * 필터의 핵심 메서드 (필터 체인)
     * 
     * @param request     Http 요청 객체
     * @param response    Http 응답 객체
     * @param filterChain 필터 체인의 나머지 필터들을 호출할 때 사용
     *
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // [1단계] 공개 API인 경우 인증 과정을 건너뛴다.
        if (isPublicApi(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // [2단계] 요청 헤더에서 JWT 토큰을 추출
        String accessToken = jwtService.extractAccessToken(request);

        // [3단계] 토큰이 존재하고 유효한 경우에만 인증 처리를 진행
        if (accessToken != null && jwtService.validateToken(accessToken)) {
            try {
                // [4단계] 토큰에서 사용자 정보를 추출
                Long userNumber = jwtService.extractUserNumberFromToken(accessToken);

                // [5단계] 토큰에서 권한 정보를 추출
                String role = jwtService.extractRoleFromRequest(request);

                // [6단계] Spring Security의 인증 객체를 생성
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userNumber, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));

                // [7단계] SecurityContext에 인증 정보를 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("인증 성공: 사용자 ID {}, 권한: {}", userNumber, role);
            } catch (ExpiredJwtException e) {
                log.warn("JWT 만료: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access Token 만료");
                return;
            } catch (MalformedJwtException | UnsupportedJwtException | SignatureException e) {
                log.warn("JWT 형식 또는 서명 오류: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "잘못된 JWT 형식 또는 서명 오류");
                return;
            } catch (IllegalArgumentException e) {
                log.warn("JWT claim 누락 또는 비어 있음: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "JWT claim 오류");
                return;
            } catch (Exception e) {
                log.error("JWT 인증 중 예기치 못한 오류: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 내부 오류");
                return;
            }
        } else {
            log.debug("유효하지 않은 JWT 토큰 또는 토큰 없음 - Location : JwtAutenticationFilter");
        }

        // [8단계] 필터 체인을 계속 진행
        filterChain.doFilter(request, response);
    }
}
