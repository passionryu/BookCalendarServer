package bookcalendar.server.Domain.Community.Controller;

import bookcalendar.server.Domain.Community.DTO.Request.PostRequest;
import bookcalendar.server.Domain.Community.Entity.Post;
import bookcalendar.server.Domain.Community.Service.CommunityService;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.response.ApiResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Community", description = "커뮤니티 관리 API")
@RestController
@RequestMapping("/api/v1/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    /**
     * 게시글 작성 API
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param postRequest 포스팅할 게시글 정보 DTO
     * @return 게시글 포스팅 성공 메시지
     */
    @Operation(summary = "게시글 작성 API",description = "게시물 추가 버튼을 누르고 데이터를 입력한 후 이 API를 작동 시키면 입력데이터가 POST DB에 저장된다. ",
    responses = {
            @ApiResponse(responseCode = "201",description = "게시글이 정상적으로 포스팅 되었습니다."),
            @ApiResponse(responseCode = "401",description = "엑세스 토큰 만료"),
            @ApiResponse(responseCode = "500",description = "서버 내부 오류 발생")
    })
    @PostMapping("/posts")
    public ResponseEntity<ApiResponseWrapper<String>> writePost(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                              @Valid @RequestBody PostRequest postRequest){

        // 게시글 작성 서비스 레이어 호출
        Integer postId = communityService.writePost(customUserDetails, postRequest);

        return ResponseEntity.status(201)
                .body(new ApiResponseWrapper<>("저장된 게시글의 postId : " + postId,"게시글이 정상적으로 포스팅 되었습니다."));
    }

    /**
     * 게시글 삭제 API
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param postId 삭제할 게시글 고유 번호
     * @return 삭제 성공 메시지 반환
     */
    @Operation(summary = "게시글 삭제 API",description = " ",
            responses = {
                    @ApiResponse(responseCode = "200",description = "해당 게시글이 정상적으로 삭제 되었습니다."),
                    @ApiResponse(responseCode = "401",description = "엑세스 토큰 만료"),
                    @ApiResponse(responseCode = "403",description = "해당 게시글에 대한 삭제 권한이 없습니다."),
                    @ApiResponse(responseCode = "500",description = "서버 내부 오류 발생")
            })
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<ApiResponseWrapper<String>> deletePost(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                 @PathVariable Integer postId){

        // 게시글 삭제 서비스 레이어 호출
        communityService.deletePost(customUserDetails, postId);

        return ResponseEntity.status(200)
                .body(new ApiResponseWrapper<>("삭제된 게시글의 postId : " + postId,"해당 게시글이 정상적으로 삭제 되었습니다."));
    }

}
