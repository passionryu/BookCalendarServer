package bookcalendar.server.Domain.Mypage.Controller;

import bookcalendar.server.Domain.Mypage.DTO.Response.UserAllInfoResponse;
import bookcalendar.server.Domain.Mypage.DTO.Response.UserSimpleInfoResponse;
import bookcalendar.server.Domain.Mypage.Service.MypageService;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.response.ApiResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Mypage", description = "마이페이지 API")
@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;

    /**
     * 간단한 유저 정보 조회 API
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 간단한 유저 정보 DTO (닉네임, 유저 랭크)
     */
    @Operation(summary = "마이페이지 간단한 유저 정보 조회 API", description = "마이페이지 상단에 닉네임과 랭크를 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "유저의 닉네임과 랭크를 정상적으로 조회했습니다."),
                    @ApiResponse(responseCode = "401", description = "유저 인증 오류"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @GetMapping("/info/simple")
    public ResponseEntity<ApiResponseWrapper<UserSimpleInfoResponse>> getUserSimpleInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        // 간단한 유저 정보 조회 서비스 레이어 호출
        UserSimpleInfoResponse userSimpleInfoResponse = mypageService.getUserSimpleInfo(customUserDetails);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(userSimpleInfoResponse, "유저의 닉네임과 랭크를 정상적으로 조회했습니다."));
    }

    /**
     * 내 프로필 상세 조회 API
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 유저의 모든 개인 정보 DTO
     */
    @Operation(summary = "내 프로필 상세 조회 API", description = "마이페이지에서 내 정보에 대한 상세조회 기능 ",
            responses = {
                    @ApiResponse(responseCode = "200", description = "유저의 정보가 정상적으로 조회했습니다."),
                    @ApiResponse(responseCode = "401", description = "유저 인증 오류"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @GetMapping("/info/all")
    public ResponseEntity<ApiResponseWrapper<UserAllInfoResponse>> getUserAllInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        // 유저의 모든 정보 조회 서비스 레이어 호출
        UserAllInfoResponse userAllInfoResponse = mypageService.getUserAllInfo(customUserDetails);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(userAllInfoResponse,"유저의 정보가 정상적으로 조회했습니다."));
    }

    @Operation(summary = "내 프로필 수정 API", description = "마이페이지의 내 프로필 상세 조회 페이지에서 프로필 수정 기능을 제공",
            responses = {
                    @ApiResponse(responseCode = "200", description = "프로필이 정상적으로 수정되었습니다."),
                    @ApiResponse(responseCode = "401", description = "유저 인증 오류"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @PatchMapping("/info")
    public ResponseEntity<ApiResponseWrapper<String>> updateUserAllInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(null,"프로필이 정상적으로 수정되었습니다."));
    }
}
