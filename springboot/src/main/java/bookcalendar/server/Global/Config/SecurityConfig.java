package bookcalendar.server.global.config;

import bookcalendar.server.global.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig {

    /**
     *
     * @param http
     * @param jwtAuthenticationFilter
     * @return
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {
        http
                // csrf 차단
                .csrf(AbstractHttpConfigurer::disable)

                // cors 설정
                .cors(cors -> cors.configurationSource(bookcalendar.server.global.config.CorsConfig.corsConfigurationSource()))

                // 시큐리티 기본 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 권한 url 설정
                .authorizeHttpRequests(req -> req.requestMatchers("/api/v1/member/register").permitAll().
                        requestMatchers("/api/v1/member/login").permitAll().
                        requestMatchers("/api/**").permitAll().

                        requestMatchers("/swagger-ui/**").permitAll().
                        requestMatchers("/swagger-ui/index.html").permitAll().
                        requestMatchers("/v3/api-docs/**").permitAll().
                        requestMatchers("/webjars/").permitAll().
                        anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // 필터 추가

        return http.build();

    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ======================= 권한 설정 로직 =========================

    // 권한계층 생성
    @Bean
    public RoleHierarchyImpl roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("""
                    ADMIN > USER
                """);
        return roleHierarchy;
    }

    // 권한 계층 등록
    @Bean
    public DefaultWebSecurityExpressionHandler customWebSecurityExpressionHandler() {
        DefaultWebSecurityExpressionHandler expressionHandler = new DefaultWebSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy());
        return expressionHandler;
    }

}
