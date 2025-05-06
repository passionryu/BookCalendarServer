package bookcalendar.server.Domain.Mypage.Controller;

import bookcalendar.server.Domain.Mypage.DTO.Request.UserInfoEditRequest;
import bookcalendar.server.Domain.Mypage.DTO.Response.*;
import bookcalendar.server.Domain.Mypage.Service.MypageService;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.response.ApiResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "Mypage", description = "마이페이지 API")
@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;

    // ======================= User Info Page =========================
    
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

    /**
     * 내 프로필 수정 API
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param userInfoEditRequest 내 프로필 데이터 수정 요청 DTO
     * @return 수정된 프로필 정보 DTO
     */
    @Operation(summary = "내 프로필 수정 API", description = "마이페이지의 내 프로필 상세 조회 페이지에서 프로필 수정 기능을 제공",
            responses = {
                    @ApiResponse(responseCode = "200", description = "프로필이 정상적으로 수정되었습니다."),
                    @ApiResponse(responseCode = "401", description = "유저 인증 오류"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @PatchMapping("/info")
    public ResponseEntity<ApiResponseWrapper<UserInfoEditResponse>> updateUserAllInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                                      @Valid @RequestBody UserInfoEditRequest userInfoEditRequest) {

        // 내 프로필 수정 서비스 레이어 호출
        UserInfoEditResponse userInfoEditResponse = mypageService.updateUserAllInfo(customUserDetails, userInfoEditRequest);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(userInfoEditResponse,"프로필이 정상적으로 수정되었습니다."));
    }
    
    // ======================= Review Page =========================

    /**
     * 내 독후감 리스트 일괄 조회 API
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 독후감 리스트 반환
     */
    @Operation(summary = "내 독후감 리스트 일괄조회 API", description = "내 독후감들을 리스트 형식으로 일괄조회하는 기능이다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "내 독후감 리스트가 정상적으로 조회되었습니다."),
                    @ApiResponse(responseCode = "401", description = "유저 인증 오류"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @GetMapping("/reviews")
    public ResponseEntity<ApiResponseWrapper<List<MyReviewList>>> getReviewList(@AuthenticationPrincipal CustomUserDetails customUserDetails){

        // 내 독후감 리스트 일괄 조회 서비스 레이어 호출
        List<MyReviewList> myReviewList = mypageService.getReviewList(customUserDetails);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(myReviewList, "내 독후감 리스트가 정상적으로 조회되었습니다."));
    }

    /**
     * 내 독후감 상세 조회 API
     *
     * @param reviewId 조회하고자 하는 독후감의 고유 번호
     * @return 독후감 기록 DTO
     */
    @Operation(summary = "내 독후감 상세조회 API", description = "독후감 리스트에서 한 독후감을 클릭하면, reviewId를 받아서 상세하게 독후감 기록을 조회하는 기능이다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "내 독후감이 정상적으로 조회되었습니다."),
                    @ApiResponse(responseCode = "401", description = "유저 인증 오류"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @GetMapping("/review")
    public ResponseEntity<ApiResponseWrapper<ReviewByReviewIdResponse>> getReview(@RequestParam("reviewId") Integer reviewId){

        // 내 독후감 상세 조회 서비스 레이어 호출
        ReviewByReviewIdResponse reviewByReviewIdResponse = mypageService.getReview(reviewId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(reviewByReviewIdResponse, "내 독후감이 정상적으로 조회되었습니다."));
    }

    /**
     * 내 독후감 삭제 API
     *
     * @param reviewId 삭제할 독후감의 고유 번호
     * @return 독후감 삭제 성공 메시지
     */
    @Operation(summary = "내 독후감 삭제 API", description = "독후감 리스트 페이지에서 삭제 버튼을 누르면 해당 독후감 기록은 reviewId 값을 받아서 Delete 메서드를 실행시킨다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "해당 독후감 기록이 정상적으로 삭제되었습니다."),
                    @ApiResponse(responseCode = "401", description = "유저 인증 오류"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @DeleteMapping("/review")
    public ResponseEntity<ApiResponseWrapper<String>> deleteReview(@RequestParam("reviewId") Integer reviewId){

        // 독후감 삭제 서비스 레이어 호출
        mypageService.deleteReview(reviewId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>("삭제한 독후감의 고유 번호 : " +reviewId , "내 독후감이 정상적으로 조회되었습니다."));
    }

    // ======================= Scrap Page =========================

    /**
     * 내 스크랩 리스트 조회 API
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 스크랩 정보 DTO 리스트
     */
    @Operation(summary = "내 스크랩 리스트 조회 API", description = "마이페이지에서 스크랩 모음을 리스트로 반환한다. 해당 페이지에서는 스크랩 리스트를 최신순으로 정렬한다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "내 스크랩 리스트가 정상적으로 반환되었습니다."),
                    @ApiResponse(responseCode = "401", description = "유저 인증 오류"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @GetMapping("/scraps")
    public ResponseEntity<ApiResponseWrapper<List<MyScrapListResponse>>> getScrapList(@AuthenticationPrincipal CustomUserDetails customUserDetails){

        // 내 스크랩 리스트 조회 서비스 레이어 호출
        List<MyScrapListResponse> myScrapList = mypageService.getScrapList(customUserDetails);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(myScrapList, "내 스크랩 리스트가 정상적으로 반환되었습니다"));
    }
    
}
