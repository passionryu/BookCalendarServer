package bookcalendar.server.Domain.Member.Controller;

import bookcalendar.server.Domain.Member.DTO.Request.RegisterRequest;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Service.MemberService;
import bookcalendar.server.global.response.ApiResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        @Operation(summary = "회원가입 API", description = "회원가입을 진행하는 API로, 필요한 정보를 받아 새로운 사용자를 등록합니다. 성공 시 memberId를 반환합니다.", responses = {
                        @ApiResponse(responseCode = "201", description = "회원가입 성공"),
                        @ApiResponse(responseCode = "409", description = "전화번호 혹은 닉네임의 중복 ")
        })
        @PostMapping("/register")
        public ResponseEntity<ApiResponseWrapper<Member>> register(
                        @RequestBody @Valid RegisterRequest registerRequest) {

                // 회원가입 서비스 레이어 호출
                Member member = memberService.register(registerRequest);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(new ApiResponseWrapper<>(member, "회원가입이 정상적으로 완료되었습니다"));
        }

}
