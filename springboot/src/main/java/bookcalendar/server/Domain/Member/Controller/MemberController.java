package bookcalendar.server.Domain.Member.Controller;

import bookcalendar.server.Domain.Member.DTO.Request.LoginRequest;
import bookcalendar.server.Domain.Member.DTO.Request.RegisterRequest;
import bookcalendar.server.Domain.Member.DTO.Request.TokenRequest;
import bookcalendar.server.Domain.Member.DTO.Response.RankResponse;
import bookcalendar.server.Domain.Member.DTO.Response.TokenResponse;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Service.MemberService;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.response.ApiResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Member", description = "회원관리 API")
@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {

        private final MemberService memberService;

        /**
         * 회원가입 API
         *
         * @param registerRequest 회원가입 데이터
         * @return Member 객체 + 회원가입 성공 메시지
         */
        @Operation(summary = "회원가입 API", description = "회원가입을 진행하는 API로, 필요한 정보를 받아 새로운 사용자를 등록합니다. 성공 시 memberId를 반환합니다.",
                responses = {
                        @ApiResponse(responseCode = "201", description = "회원가입 성공"),
                        @ApiResponse(responseCode = "409", description = "전화번호 혹은 닉네임의 중복 "),
                        @ApiResponse(responseCode = "500", description = "서버 내부 오류 ")
                })
        @PostMapping("/register")
        public ResponseEntity<ApiResponseWrapper<Member>> register(@RequestBody @Valid RegisterRequest registerRequest) {

                // 회원가입 서비스 레이어 호출
                Member member = memberService.register(registerRequest);

                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new ApiResponseWrapper<>(member, "회원가입이 정상적으로 완료되었습니다"));
        }

        /**
         * 로그인 API
         *
         * @param loginRequest 로그인 데이터
         * @return JWT AccessToken + 로그인 성공 메시지
         */
        @Operation(summary = "로그인 API",description = "로그인을 진행하는 API로 닉네임과 비밀번호를 받고 클라이언트에게 AccessToken을 반환하고, Redis-session 저장소에 RefreshToken을 저장한다.",
                responses = {
                        @ApiResponse(responseCode = "200", description = "로그인 성공"),
                        @ApiResponse(responseCode = "401", description = "비밀번호가 일치하지 않습니다."),
                        @ApiResponse(responseCode = "404", description = "해당 유저를 찾울 수 없음"),
                        @ApiResponse(responseCode = "500", description = "서버 내부 오류 ")
                })
        @PostMapping("/login")
        public ResponseEntity<ApiResponseWrapper<TokenResponse>> login(@RequestBody LoginRequest loginRequest){

                // 로그인 서비스 레이어 호출
                TokenResponse jwtToken = memberService.login(loginRequest);

                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponseWrapper<>(jwtToken,"정상적으로 로그인에 성공하였습니다."));
        }

        /**
         * 로그아웃 API
         *
         * @param httpServletRequest
         * @return 로그아웃 성공 메시지
         */
        @Operation(summary = "로그아웃 API",description = "로그 아웃 진행 시, 클라이언트 레이어에서는 엑세스 토큰을 삭제하고 서버 레이어에서는 Session Redis에 블랙리스트틀 만들어 해당 유저의 현재 엑세스 토큰을 블랙리스트에 업데이트한다.",
                responses = {
                        @ApiResponse(responseCode = "200", description = "정상적으로 로그아웃에 성공하였습니다."),
                        @ApiResponse(responseCode = "404", description = "해당 유저를 찾울 수 없음"),
                        @ApiResponse(responseCode = "500", description = "서버 내부 오류 ")
                })
        @PostMapping("/logout")
        public ResponseEntity<ApiResponseWrapper<Void>> logout(HttpServletRequest httpServletRequest) {

                // 로그아웃 서비스 레이어 호출
                memberService.logout(httpServletRequest);

                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponseWrapper<>(null,"정상적으로 로그아웃에 성공하였습니다."));
        }

        /**
         * JWT 토큰 최신화 API (RTR)
         *
         * @param tokenRequest 유저가 전송한 토큰
         * @return JWT 토큰 DTO + 최신화 성공 메시지
         */
        @Operation(summary = "RTR API", description = "엑세스 토큰 만료시, 클라이언트 측에서 body에 리프레시 토큰을 담아 엑세스토큰, 리프레시 토큰 재발급을 요청하는 API.",
                responses = {
                        @ApiResponse(responseCode = "201", description = "토큰 RTR 재발급 성공"),
                        @ApiResponse(responseCode = "401", description = "요청한 리프레시 토큰과 저장된 리프레시 토큰이 일치하지 않습니다."),
                        @ApiResponse(responseCode = "500", description = "서버 내부 오류 ")
                })
        @PostMapping("/rtr")
        public ResponseEntity<ApiResponseWrapper<TokenResponse>> refreshToken(@RequestBody TokenRequest tokenRequest){

                // 토큰 최신화 서비스 레이어 호출
                TokenResponse tokenResponse = memberService.refreshToken(tokenRequest);

                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new ApiResponseWrapper<>(tokenResponse, "엑세스 토큰 & 리프레시 토큰이 정상적으로 재발급 되었습니다."));
        }

}
