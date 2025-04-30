package bookcalendar.server.Domain.Community.Controller;

import bookcalendar.server.Domain.Community.DTO.Request.CommentRequest;
import bookcalendar.server.Domain.Community.DTO.Request.PostRequest;
import bookcalendar.server.Domain.Community.DTO.Response.CommentResponse;
import bookcalendar.server.Domain.Community.DTO.Response.PostListResponse;
import bookcalendar.server.Domain.Community.DTO.Response.PostResponse;
import bookcalendar.server.Domain.Community.Service.CommunityService;
import bookcalendar.server.Domain.Member.DTO.Response.RankResponse;
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

    /**
     * 유저 메달 및 랭킹 반환
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 유저 메달 & 랭킹 정보 + 반환 성공 메시지
     */
    @Operation(summary = " 유저 메달 및 랭킹 반환 API", description = "유저가 커뮤니티 등에서 사용할 메달에 대한 정보와 랭킹에 대한 정보를 반환한다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "유저의 메달 및 랭킹 정상적으로 반환"),
                    @ApiResponse(responseCode = "401",description = "엑세스 토큰 만료"),
                    @ApiResponse(responseCode = "404", description = "해당 유저를 찾을 수 없습니다."),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @GetMapping("/rank")
    public ResponseEntity<ApiResponseWrapper<RankResponse>> getRank(@AuthenticationPrincipal CustomUserDetails customUserDetails){

        // 유저 메달 및 랭킹 반환 서비스 레이어 호출
        RankResponse rankResponse = communityService.getRank(customUserDetails);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(rankResponse,"유저 메달 및 랭킹에 대한 정보가 정상적으로 반환되었습니다."));
    }

    /**
     * 커뮤니티 게시글 리스트 반환 API
     *
     * @return 커뮤니티 게시글 리스트
     */
    @Operation(summary = " 커뮤니티 게시글 리스트 반환 API", description = "커뮤니티에 입장하면 자동으로 해당 API를 작동하여, 유저에게 게시글 리스트를 보여준다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "커뮤니티 게시글 리스트가 정상적으로 조회되었습니다."),
                    @ApiResponse(responseCode = "401",description = "엑세스 토큰 만료"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @GetMapping("/lists")
    public ResponseEntity<ApiResponseWrapper<List<PostListResponse>>> getPostList(){

        // 게시글 리스트 반환 서비스 레이어 호출
        List<PostListResponse> postList = communityService.getPostList();

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(postList,"커뮤니티 게시글 리스트가 정상적으로 조회되었습니다."));
    }

    /**
     * 선택한 게시글 상세조회 API
     *
     * @param postId 게시글 고유 번호
     * @return 게시글 정보 반환
     */
    @Operation(summary = " 선택한 게시글 상세조회 API", description = "postId를 입력받아 해당 게시글의 페이지 정보를 반환한다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "커뮤니티 게시글이 정상적으로 조회되었습니다."),
                    @ApiResponse(responseCode = "401",description = "엑세스 토큰 만료"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @GetMapping("/lists/{postId}")
    public ResponseEntity<ApiResponseWrapper<PostResponse>> getPostDetail(@PathVariable Integer postId){

        // 선택한 게시글 정보 반환 서비스 레이어 호출
        PostResponse postResponse = communityService.getPostDetail(postId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(postResponse,"커뮤니티 게시글이 정상적으로 조회되었습니다."));
    }

    /**
     * 댓글 작성 API
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param postId 게시글 고유 번호
     * @param commentRequest 댓글 요청 데이터
     * @return commentId를 반환해서 작성 후 바로 페이지에 랜딩될 수 있도록 함
     */
    @Operation(summary = " 댓글 작성 API", description = "postId를 입력받고 해당 게시글에 댓글을 저장하며 작성과 동시에 페이지에 바로 랜딩 될 수 있도록 한다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "댓글이 정상적으로 포스팅 되었습니다."),
                    @ApiResponse(responseCode = "401",description = "엑세스 토큰 만료"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponseWrapper<Void>> createComment(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                         @PathVariable Integer postId, @Valid @RequestBody CommentRequest commentRequest){

        // 댓글 작성 서비스 레이어 호출
        communityService.createComment(customUserDetails, postId, commentRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseWrapper<>(null,"댓글이 정상적으로 포스팅 되었습니다."));
    }

    /**
     * 게시물에서 댓글들 조회 API
     *
     * @param postId 댓글이 달린 게시글 고유 번호
     * @return 댓글 리스트
     */
    @Operation(summary = " 게시물에서 댓글들 조회 API ",description = "커뮤니티에서 한 게시글을 조회할 떄, 그때 댓글을 조회할 기능이다.",
    responses = {
            @ApiResponse(responseCode = "200", description = "게시물에 댓글이 정상적으로 조회되었습니다."),
            @ApiResponse(responseCode = "401",description = "엑세스 토큰 만료"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponseWrapper<List<CommentResponse>>> getComments(@PathVariable Integer postId){

        // 게시물에서 댓글 리스트 반환 서비스 레이어 호출
        List<CommentResponse> commentResponseList = communityService.getCommentList(postId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(commentResponseList, "게시물에 댓글이 정상적으로 조회되었습니다."));
    }

}
