package bookcalendar.server.Global.JWT;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /* 권한이 필요 없는 API Path 선언 */
    private static final List<String> PUBLIC_APIS = List.of(


    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {



        filterChain.doFilter(request, response);

    }

    /**
     * 권한 불필요 API들은 넘기는 메서드
     * @param request 사용자 요청
     * @return ture/false
     */
    private boolean isPublicApi(HttpServletRequest request) {
        String path = request.getRequestURI();
        //로그 출력 -> path /login/id이렇게 나와서 아마 ....그런건가
        return PUBLIC_APIS.stream().anyMatch(path::startsWith);
    }
}
