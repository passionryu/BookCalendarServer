package bookcalendar.server.Domain.Community.Controller;

import bookcalendar.server.Domain.Community.DTO.Request.CommentRequest;
import bookcalendar.server.Domain.Community.DTO.Request.PostRequest;
import bookcalendar.server.Domain.Community.DTO.Response.CommentResponse;
import bookcalendar.server.Domain.Community.DTO.Response.PostListResponse;
import bookcalendar.server.Domain.Community.DTO.Response.PostResponse;
import bookcalendar.server.Domain.Community.DTO.Response.TopLikedPosts;
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

    // ======================= 게시글 영역 =========================

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

        long start = System.currentTimeMillis(); // 시간 측정 시작

        // 게시글 리스트 반환 서비스 레이어 호출
        List<PostListResponse> postList = communityService.getPostList();

        long end = System.currentTimeMillis(); // 시간 측정 종료
        long duration = end - start;
        log.info("[List<PostListResponse>] 처리 시간: {}ms", duration); // 로그 출력

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
    public ResponseEntity<ApiResponseWrapper<PostResponse>> getPostDetail(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                          @PathVariable Integer postId){

        // 선택한 게시글 정보 반환 서비스 레이어 호출
        PostResponse postResponse = communityService.getPostDetail(customUserDetails,postId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(postResponse,"커뮤니티 게시글이 정상적으로 조회되었습니다."));
    }

    // ======================= 댓글 영역 =========================

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

    /**
     * 내 댓글 삭제 API
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param commentId 삭제하고자 하는 댓글의 고유 번호
     * @return 댓글 삭제 성공 메시지
     */
    @Operation(summary = " 내 댓글 삭제 API ",description = "삭제 버튼을 누른 댓글이 내 댓글일 경우 삭제",
            responses = {
                    @ApiResponse(responseCode = "200", description = "내 댓글이 성공적으로 삭제되었습니다."),
                    @ApiResponse(responseCode = "401",description = "엑세스 토큰 만료"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @DeleteMapping(("/comments/{commentId}"))
    public ResponseEntity<ApiResponseWrapper<Void>> deleteComment(@AuthenticationPrincipal CustomUserDetails customUserDetails ,
                                                                  @PathVariable Integer commentId){

        // 선택한 내 댓글 삭제 서비스 레이어 호출
        communityService.deleteComment(customUserDetails, commentId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(null, "내 댓글이 성공적으로 삭제되었습니다."));
    }

    /**
     * 게시글 작성자 본인 게시글의 댓글 삭제 API
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param postId 삭제할 댓글의 게시글 고유 번호
     * @param commentId 삭제할 댓글의 고유 번호
     * @return 댓글 삭제 성공 메시지
     */
    @Operation(summary = " 게시글 작성자의 본인 게시글 내의 댓글 삭제 API ",description = "본인이 작성한 게시글일 경우 해당 게시글의 댓글들은 삭제할 권한이 있다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "댓글이 성공적으로 삭제되었습니다."),
                    @ApiResponse(responseCode = "401",description = "엑세스 토큰 만료"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResponseWrapper<Void>> deleteCommentByPostOwner(@AuthenticationPrincipal CustomUserDetails customUserDetails ,
                                                                             @PathVariable Integer postId ,
                                                                             @PathVariable Integer commentId){

        // 게시글 작성자 본인 게시글 내의 댓글 삭제 서비스 레이어 호출
        communityService.deleteCommentByPostOwner(customUserDetails, postId, commentId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(null, "댓글이 성공적으로 삭제되었습니다."));
    }

    // ======================= 신고 영역 =========================

    /**
     * 게시글 신고 API
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param postId 게시글 고유 번호
     * @return 신고 성공 메시지
     */
    @Operation(summary = "게시글 신고 API",description = "게시글 신고 버튼을 누르면, DB에 신고 count가 1 증가함, 다만 동일인이 중복 신고는 불가능",
    responses  ={
        @ApiResponse(responseCode = "200", description = "게시글이 성공적으로 신고되었습니다."),
        @ApiResponse(responseCode = "401",description = "엑세스 토큰 만료"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/posts/{postId}/reports")
    public ResponseEntity<ApiResponseWrapper<String>> reportPost(@AuthenticationPrincipal CustomUserDetails customUserDetails ,
                                                               @PathVariable Integer postId){

        // 게시글 신고 서비스 레이어 호출
        communityService.reportPost(customUserDetails, postId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>("신고한 게시글 고유 번호 : " + postId, "게시글이 성공적으로 신고되었습니다."));
    }

    /**
     * 댓글 신고 API
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param commentId 신고하고자 하는 댓글 고유 번호
     * @return 댓글 신고 성공 메시지
     */
    @Operation(summary = "댓글 신고 API",description = "댓글 신고 버튼을 누르면, DB에 신고 count가 1 증가함, 다만 동일인이 중복 신고는 불가능",
            responses  ={
                    @ApiResponse(responseCode = "200", description = "댓글이 성공적으로 신고되었습니다."),
                    @ApiResponse(responseCode = "401",description = "엑세스 토큰 만료"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @PostMapping("/comments/{commentId}/reports")
    public ResponseEntity<ApiResponseWrapper<String>> reportComment(@AuthenticationPrincipal CustomUserDetails customUserDetails ,
                                                                 @PathVariable Integer commentId){

        // 댓글 신고 서비스 레이어 호출
        communityService.reportComment(customUserDetails, commentId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>("신고한 댓글 고유 번호 : " + commentId, "댓글이 성공적으로 신고되었습니다."));
    }

    // ======================= 스크랩 영역 =========================

    /**
     * 게시글 스크랩 API
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param postId 스크랩 하고자 하는 게시글 고유 번호
     * @return
     */
    @Operation(summary = "게시글 스크랩 API",description = "게시글 스크랩 버튼을 누르면 해당 post의 postId가 scrap DB테이블에 저장된다.",
            responses  ={
                    @ApiResponse(responseCode = "200", description = "게시글이 성공적으로 스크랩되었습니다."),
                    @ApiResponse(responseCode = "401",description = "엑세스 토큰 만료"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @PostMapping("/posts/{postId}/scraps")
    public ResponseEntity<ApiResponseWrapper<String>> scrapPost(@AuthenticationPrincipal CustomUserDetails customUserDetails ,
                                                               @PathVariable Integer postId){

        // 게시글 스크랩 서비스 레이어 호출
        communityService.scrapPost(customUserDetails, postId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>("스크랩한 게시글 고유 번호 : " + postId, "게시글이 성공적으로 스크랩되었습니다."));
    }

    // ======================= 검색 영역 =========================

    /**
     * 게시글 검색 API
     *
     * @param keyword 검색 키워드
     * @return 검색된 게시글 리스트
     */
    @Operation(summary = "게시글 검색 API",description = "커뮤니티 메인 페이지 상단의 검색창의 게시글 검색 기능이다. 아직은 기본적인 기능만 구현하였으며 추후 게시글 || 작성자 검색 + 자동완성 기능까지 연장할 계획이다.",
            responses  ={
                    @ApiResponse(responseCode = "200", description = "게시글이 성공적으로 검색되었습니다."),
                    @ApiResponse(responseCode = "401",description = "엑세스 토큰 만료"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @PostMapping("/search")
    public ResponseEntity<ApiResponseWrapper<List<PostListResponse>>> searchPost(@RequestParam String keyword){

        List<PostListResponse> postList = communityService.searchPost(keyword);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(postList, "게시글이 성공적으로 검색되었습니다."));
    }

    // ======================= 게시글 Like 영역 =========================

    /**
     * Like 버튼 누르기 API
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 좋아요 총 합산 수 반환
     */
    @Operation(summary = "Like 버튼 누르기 API",description = "좋아요 버튼을 누르는 기능, 한번 더 누르면 좋아요 취소 기능까지 포함되어 있으며 해당 API가 작동되면 DB작업과 함께 클라이언트에게 좋아요 총합 수 를 반환한다.",
            responses  ={
                    @ApiResponse(responseCode = "200", description = "게시글에 성공적으로 좋아요 버튼이 클릭되었습니다."),
                    @ApiResponse(responseCode = "401",description = "엑세스 토큰 만료"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @PostMapping("/like/{postId}")
    public ResponseEntity<ApiResponseWrapper<Integer>> clickLike(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                 @PathVariable Integer postId){

        // Like 버튼 누르기 서비스 레이어 호출
        Integer likeCount = communityService.clickLike(customUserDetails, postId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(likeCount, "게시글에 성공적으로 좋아요 버튼이 클릭되었습니다."));
    }

    /**
     * LikeCount 총 합산 반환 API
     *
     * @param postId 게시글 고유 번호
     * @return LikeCount 수
     */
    @Operation(summary = "Like 합산 반환 API",description = "게시글 조회시 바로 반환되는 좋아요 총 합산",
            responses  ={
                    @ApiResponse(responseCode = "200", description = "게시글에 좋아요 총 합산이 성공적으로 반환되었습니다."),
                    @ApiResponse(responseCode = "401",description = "엑세스 토큰 만료"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @GetMapping("/like/{postId}")
    public ResponseEntity<ApiResponseWrapper<Integer>> getLikeCount(@PathVariable Integer postId){

        Integer likeCount = communityService.getLikeCount(postId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(likeCount, "게시글에 좋아요 총 합산이 성공적으로 반환되었습니다."));
    }

    /**
     * Like 수 Top3 게시글 썸네일 리스트 반환 API
     *
     * @return Like 수 Top3 게시글 썸네일 리스트
     */
    @Operation(summary = " Like 수 Top3 게시글 썸네일 리스트 반환 API ",description = "Top 3 게시글 썸네일 반환",
            responses  ={
                    @ApiResponse(responseCode = "200", description = "Top3 게시글이 성공적으로 반환되었습니다."),
                    @ApiResponse(responseCode = "401",description = "엑세스 토큰 만료"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            })
    @GetMapping("/posts/top-liked")
    public ResponseEntity<ApiResponseWrapper<List<TopLikedPosts>>> getTopLikedPosts(){

        long start = System.currentTimeMillis(); // 시간 측정 시작

        // Like 수 top3 게시글 썸네일 리스트 반환 서비스 레이어 호출
        List<TopLikedPosts> topLikedPostsList = communityService.getTopLikedPosts();

        long end = System.currentTimeMillis(); // 시간 측정 종료
        long duration = end - start;
        log.info("[top3 posts] 처리 시간: {}ms", duration); // 로그 출력

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseWrapper<>(topLikedPostsList, "Top3 게시글이 성공적으로 반환되었습니다."));
    }

}
