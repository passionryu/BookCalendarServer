package bookcalendar.server.Domain.Mypage.Controller;

import bookcalendar.server.Domain.Community.DTO.Response.PostResponse;
import bookcalendar.server.Domain.Mypage.DTO.Request.ManualCartRequest;
import bookcalendar.server.Domain.Mypage.DTO.Request.UserInfoEditRequest;
import bookcalendar.server.Domain.Mypage.DTO.Response.*;
import bookcalendar.server.Domain.Mypage.Entity.Cart;
import bookcalendar.server.Domain.Mypage.Service.MypageService;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.response.ApiResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.SearchRequest;
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
    @GetMapping("/review/{reviewId}")
    public ResponseEntity<ApiResponseWrapper<ReviewByReviewIdResponse>> getReview(@PathVariable("reviewId") Integer reviewId){

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
    @DeleteMapping("/review/{reviewId}")
    public ResponseEntity<ApiResponseWrapper<String>> deleteReview(@PathVariable("reviewId") Integer reviewId){

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

    /**
     * 내 스크랩 상세 상세 조회 API
     *
     * @param scrapId 스크랩 하고자 하는 스크랩 객체 고유 번호
     * @return 스크랩 한 게시글
     */
    @Operation(summary = "내 스크랩 상세 조회 API", description = "스크랩 리스트 페이지에서 scrapId를 통해 상세 정보를 반환한다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "스크랩 정보가 정상적으로 반환되었습니다."),
                    @ApiResponse(responseCode = "401", description = "유저 인증 오류"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @GetMapping("/scrap/{scrapId}")
    public ResponseEntity<ApiResponseWrapper<PostResponse>> getScrap(@PathVariable("scrapId") Integer scrapId){

        // 스크랩 한 게시글 정보 반환 서비스 레이어 호출
        PostResponse postResponse = mypageService.getScrapDetail(scrapId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(postResponse, "스크랩 정보가 정상적으로 반환되었습니다."));
    }

    /**
     * 내 스크랩 취소 API
     *
     * @param scrapId 취소하고자 하는 스크랩의 고유 번호
     * @return 삭제 성공 메시지
     */
    @Operation(summary = "내 스크랩 취소 API", description = "스크랩 리스트 페이지에서 scrapId를 통해 상세 정보를 반환한다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "스크랩이 정상적으로 취소 되었습니다."),
                    @ApiResponse(responseCode = "401", description = "유저 인증 오류"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @DeleteMapping("/scrap/{scrapId}")
    public ResponseEntity<ApiResponseWrapper<String>> deleteScrap(@PathVariable("scrapId") Integer scrapId){

        // 스크랩 취소 서비스 레이어 호출
        mypageService.deleteScrap(scrapId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>("취소한 스크랩 고유 번호 : " + scrapId, "스크랩이 정상적으로 취소 되었습니다."));
    }

    // ======================= Cart Page =========================

    /**
     * 장바구니에 책 수동 등록 API
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param manualCartRequest 장바구니 저장 정보 DTO
     * @return 장바구니 저장 성공 메시지
     */
    @Operation(summary = "장바구니에 책 수동 등록 API", description = "마이페이지 > 장바구니에서 수동으로 직접 책을 등록 ",
            responses = {
                    @ApiResponse(responseCode = "200", description = "장바구니에 정상적으로 책이 등록되었습니다."),
                    @ApiResponse(responseCode = "401", description = "유저 인증 오류"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @PostMapping("/cart")
    public ResponseEntity<ApiResponseWrapper<Cart>> saveBookToCartByManual(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                             @RequestBody ManualCartRequest manualCartRequest){

        // 장바구니 수동 등록 서비스 레이어 호츌
        Cart cart =mypageService.saveBookToCartByManual(customUserDetails, manualCartRequest);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(cart,"장바구니에 정상적으로 책이 등록되었습니다."));
    }

    /**
     * 장바구니 일괄 조회 API
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 장바구니 DTO 리스트
     */
    @Operation(summary = "장바구니 일괄조회 API", description = "마이페이지 > 장바구니에서 장바구니 리스트 조회 ",
            responses = {
                    @ApiResponse(responseCode = "200", description = "장바구니의 책이 정상적으로 조회 되었습니다."),
                    @ApiResponse(responseCode = "401", description = "유저 인증 오류"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @GetMapping("/cart")
    public ResponseEntity<ApiResponseWrapper<List<Cart>>> getCartList(@AuthenticationPrincipal CustomUserDetails customUserDetails){

        // 장바구니 일괄 조회 서비스 레이어 호출
        List<Cart> cartList = mypageService.getCartList(customUserDetails);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(cartList,"장바구니의 책이 정상적으로 조회 되었습니다."));
    }

    /**
     * 저장된 장바구니 도서 취소 API
     *
     * @param cartId 장바구니 객체 고유 번호
     * @return 장바구니 취소 성공 메시지
     */
    @Operation(summary = "저장된 장바구니 도서 취소 API", description = "마이페이지 > 장바구니에서 장바구니 리스트에서 삭제 버튼을 통해 도서 취소 ",
            responses = {
                    @ApiResponse(responseCode = "200", description = "장바구니의 책이 정상적으로 취소 되었습니다."),
                    @ApiResponse(responseCode = "401", description = "유저 인증 오류"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @DeleteMapping("/cart/{cartId}")
    public ResponseEntity<ApiResponseWrapper<String>> deleteCart(@PathVariable("cartId") Integer cartId){


        // 저장된 장바구니 도서 취소 서비스 레이어 호출
        mypageService.deleteCart(cartId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>("장바구니 취소된 도서의 장바구니 고유 번호 : " + cartId,"장바구니의 책이 정상적으로 취소 되었습니다."));
    }

    // ======================= Search Engine(검색 엔진) =========================

    /**
     * 독후감 검색 API
     *
     * @param keyword 검색 키워드
     * @return 검색된 독후감 리스트
     */
//    @Operation(summary = "독후감 검색 API",description = "독후감 검색 기능이다.",
//            responses  ={
//                    @ApiResponse(responseCode = "200", description = "입력하신 키워드로 독후감이 성공적으로 검색되었습니다."),
//                    @ApiResponse(responseCode = "401",description = "엑세스 토큰 만료"),
//                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
//            })
//    @PostMapping("/search/review")
//    public ResponseEntity<ApiResponseWrapper<List<MyReviewList>>> searchReview(@RequestParam String keyword){
//
//
//        return ResponseEntity.status(HttpStatus.OK)
//                .body(new ApiResponseWrapper<>(null, "입력하신 키워드로 독후감이 성공적으로 검색되었습니다."));
//    }
}
