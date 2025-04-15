package bookcalendar.server.Domain.Member.Controller;

import bookcalendar.server.Domain.Member.DTO.Request.LoginRequest;
import bookcalendar.server.Domain.Member.DTO.Request.RegisterRequest;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Service.MemberService;
import bookcalendar.server.global.response.ApiResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
                        @ApiResponse(responseCode = "409", description = "전화번호 혹은 닉네임의 중복 ")
                })
        @PostMapping("/register")
        public ResponseEntity<ApiResponseWrapper<Member>> register(@RequestBody @Valid RegisterRequest registerRequest) {

                // 회원가입 서비스 레이어 호출
                Member member = memberService.register(registerRequest);

                return ResponseEntity
                        .status(HttpStatus.CREATED)
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
                        @ApiResponse(responseCode = "404", description = "해당 유저를 찾울 수 없음")
                   })
        @PostMapping("/login")
        public ResponseEntity<ApiResponseWrapper<String>> login(@RequestBody LoginRequest loginRequest){

                // 로그인 서비스 레이어 호출
                String jwtAccessToken = memberService.login(loginRequest);
                log.info("jwtAccessToken: {}", jwtAccessToken);

                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(new ApiResponseWrapper<>(jwtAccessToken,"정상적으로 로그인에 성공하였습니다."));
        }

        /* 유저 메달 및 랭킹 반환 */

}
