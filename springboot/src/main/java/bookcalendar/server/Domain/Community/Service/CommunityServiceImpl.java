package bookcalendar.server.Domain.Community.Service;

import bookcalendar.server.Domain.Community.DTO.Request.CommentRequest;
import bookcalendar.server.Domain.Community.DTO.Request.PostRequest;
import bookcalendar.server.Domain.Community.DTO.Response.CommentResponse;
import bookcalendar.server.Domain.Community.DTO.Response.PostListResponse;
import bookcalendar.server.Domain.Community.DTO.Response.PostResponse;
import bookcalendar.server.Domain.Community.DTO.Response.TopLikedPosts;
import bookcalendar.server.Domain.Community.Entity.*;
import bookcalendar.server.Domain.Community.Exception.CommunityException;
import bookcalendar.server.Domain.Community.Helper.CommunityHelper;
import bookcalendar.server.Domain.Community.Manager.CommunityManager;
import bookcalendar.server.Domain.Community.Mapper.CommunityMapper;
import bookcalendar.server.Domain.Community.Repository.*;
import bookcalendar.server.Domain.Member.DTO.Response.RankResponse;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ScrapRepository scrapRepository;
    private final CommunityManager communityManager;
    private final CommunityMapper communityMapper;
    private final PostReportRepository postReportRepository;
    private final CommentReportRepository commentReportRepository;
    private final PostLikeRepository postLikeRepository;
    private final CacheManager cacheManager;

    // ======================= 게시글 영역 =========================

    /* 게시글 작성 메서드 */
    @Override
    @Transactional
    @CacheEvict(value = "postList", beforeInvocation = false)
    public Integer writePost(CustomUserDetails customUserDetails, PostRequest postRequest) {

        // 현재 멤버 객체 반환
        Member member = communityManager.getMember(customUserDetails.getMemberId());

        // 입력 정보 및 유저 정보를 통해 Post 엔티티 생성
        Post post = postRepository.save(CommunityHelper.postEntityBuilder(member, postRequest));
        return post.getPostId();
    }
    /**
     *  @CacheEvict(value = "postList", beforeInvocation = false)  에 대한 설명
     *  문제 상황 : 게시글 작성 이후 게시글 리스트에 방금 작성한 게시글이 조회되지 않는 문제
     *
     * Transactional과 @CacheEvict기 함께 쓰이는 경우
     * DB에 메서드의 결괏값이 커밋 되기 이전에 캐시를 삭제한 후 게시글 리스트 조회 api를 호출하니,
     * 새 커밋 DB가 반영되지 않는 문제가 있어서
     * beforeInvocation = false를 추가해서 커밋 이후 캐시가 삭제되게 수정함
     */

    /* 게시글 삭제 메서드 */
    @Override
    @CacheEvict(value = "postList")
    public void deletePost(CustomUserDetails customUserDetails, Integer postId) {

        // 게시글 ID를 통한 게시글 객체 반환
        Post post = communityManager.getPost(postId);

        // 현재 유저가 삭제하려는 게시글에 삭제 권한이 있는지 확인
        CommunityHelper.checkOwnership_post(customUserDetails, post);

        // 해당 post 객체 삭제
        postRepository.delete(post);
    }

    /* 랭크 반환 메서드 */
    @Override
    @Cacheable(value = "rankCache", key = "#customUserDetails.memberId")
    public RankResponse getRank(CustomUserDetails customUserDetails) {

        // 인증된 유저 객체의를 활용한 Member 객체 반환
        Member member = communityManager.getMember(customUserDetails.getMemberId());

        // 유저 메달 정보 & 랭킹 반환
        return new RankResponse(member.getNickName(), member.getRank(), member.getReviewCount());
    }

    /* 게시글 리스트 반환 메서드 */
    @Override
    @Cacheable(value = "postList")
    public List<PostListResponse> getPostList() {

        log.info("==> Cache Miss (게시글 리스트 반환) : DB에서 게시글 리스트 정보를 가져옵니다.");

        // 게시글 리스트 DTO 반환
        return postRepository.findAllPostSummaries();
    }

    /* 게시글 상세 조회 메서드 */
    @Override
    public PostResponse getPostDetail(CustomUserDetails customUserDetails, Integer postId) {

        Member member = communityManager.getMember(customUserDetails.getMemberId());
        Post post = communityManager.getPost(postId);

        PostResponse postResponse =postRepository.getPostDetail(postId)
                .orElseThrow(() -> new CommunityException(ErrorCode.POST_NOT_FOUND));

        if(postLikeRepository.existsByPostAndMember(post,member))
            postResponse.setClicked(true);

        // 선택한 게시글 상세 내용 반환
        return postResponse;
    }

    // ======================= 댓글 영역 =========================

    /* 댓글 생성 메서드 */
    @Override
    public void createComment(CustomUserDetails customUserDetails, Integer postId, CommentRequest commentRequest) {

        // member,post 객체와 댓글 데이터를 입력하여 댓글 객체 생성
        Comment comment = CommunityHelper.commentEntityBuilder(
                communityManager.getMember(customUserDetails.getMemberId()),
                communityManager.getPost(postId),
                commentRequest);

        // 댓글 객체 저장
        commentRepository.save(comment);
    }

    /* 해당 게시글의 모든 댓글 반환 메서드 */
    @Override
    public List<CommentResponse> getCommentList(Integer postId) {

        // 게시글에서 댓글 리스트 반환 매퍼 호출
        return communityMapper.getCommentsByPostId(postId);
    }

    /* 내 댓글 삭제 메서드 */
    @Override
    public void deleteComment(CustomUserDetails customUserDetails, Integer commentId) {

        Comment comment = communityManager.getComment(commentId);

        CommunityHelper.checkOwnership_comment(customUserDetails, comment); // 현재 유저가 삭제하려는 댓글에 삭제 권한이 있는지 확인
        commentRepository.delete(comment);
    }

    /* 게시글 작성자의 해당 게시글의 모든 댓글 삭제 메서드 */
    @Override
    public void deleteCommentByPostOwner(CustomUserDetails customUserDetails, Integer postId, Integer commentId) {

        Comment comment = communityManager.getComment(commentId);
        Post post = communityManager.getPost(comment.getPost().getPostId());

        CommunityHelper.checkOwnership_post(customUserDetails, post); // 위 게시글의 작성자가 현재 유저와 동일한지 검증 - 삭제 권한 확인
        commentRepository.delete(comment);
    }

    // ======================= 신고 영역 =========================

    /* 게시글 신고 메서드 */
    @Override
    @Transactional
    public void reportPost(CustomUserDetails customUserDetails, Integer postId) {

        Post post = communityManager.getPost(postId);

        // 중복 검사 - 기존에 해당 게시글을 신고한 기록이 있는지 확인
        if(postReportRepository.existsByPostAndMember(post, communityManager.getMember(customUserDetails.getMemberId()))){
         throw new CommunityException(ErrorCode.ALREADY_REPORT_POST);
        }

        post.increaseReportCount(); // 도메인 객체에서 신고 수 1 증가
        postReportRepository.save(PostReport.builder().
                post(post).
                member(communityManager.getMember(customUserDetails.getMemberId())).
                reportDate(LocalDateTime.now()).
                build()); // 신고 기록 저장
    }

    /* 댓글 신고 메서드 */
    @Override
    @Transactional
    public void reportComment(CustomUserDetails customUserDetails, Integer commentId) {

        Comment comment = communityManager.getComment(commentId);

        // 중복 검사 - 기존에 해당 댓글을 신고한 기록이 있는지 확인
        if(commentReportRepository.existsByCommentAndMember(comment, communityManager.getMember(customUserDetails.getMemberId()))){
            throw new CommunityException(ErrorCode.ALREADY_REPORT_COMMENT);
        }
        comment.increaseReportCount(); // 도메인 객체에서 신고수 1 증가
        commentReportRepository.save(CommentReport.builder().
                comment(comment).
                member(communityManager.getMember(customUserDetails.getMemberId())).
                reportDate(LocalDateTime.now()).
                build()); // 신고 기록 저장
    }

    // ======================= 스크랩 영역 =========================

    /* 게시글 스크랩 메서드 */
    @Override
    @Transactional
    @CacheEvict(value = "myScrapList", key = "#customUserDetails.memberId")
    public void scrapPost(CustomUserDetails customUserDetails, Integer postId) {

        Member member = communityManager.getMember(customUserDetails.getMemberId());
        Post post = communityManager.getPost(postId);

        if(scrapRepository.existsByMember_MemberIdAndPost_PostId(member.getMemberId(), post.getPostId())){
            throw new CommunityException(ErrorCode.ALREADY_SCRAP);
        }

        Scrap scrap = CommunityHelper.scrapEntityBuilder(member, post); // Helper 클래스에서 스크랩 객체 생성 레이어 호출
        scrapRepository.save(scrap); // Scrap 객체 저장
    }

    // ======================= 검색창 영역 =========================

    /* 커뮤니티 검색창 검색 메서드 */
    @Override
    public List<PostListResponse> searchPost(String keyword) {

        return postRepository.searchPostsByKeyword(keyword);
    }

    // ======================= 게시글 Like 영역 =========================

    /* Like 버튼 누르기 메서드 */
    @Override
    @Transactional
    public Integer clickLike(CustomUserDetails customUserDetails, Integer postId) {

        Member member = communityManager.getMember(customUserDetails.getMemberId()); // Like 버튼을 누른 멤버 객체 반환
        Post post = communityManager.getPost(postId); // Like 버튼을 누를 게시글 객체 반환

        /* 조건문 - 기존에 DB기록 조회에 따른, DB쿼리 작업 */
        if(postLikeRepository.existsByPostAndMember(post,member)){
            postLikeRepository.deleteByPostAndMember(post,member); // 좋아요 기록 삭제
        }
        else {
            postLikeRepository.save(
                    PostLike.builder().post(post).member(member).build()
            );
        }

        // 현재 게시글의 좋아요 총합
        int newLikeCount = postLikeRepository.countByPost(post);

        // 현재 캐시에 저장된 Top3 리스트 가져오기
        List<TopLikedPosts> cachedTop3 = (List<TopLikedPosts>) cacheManager.getCache("top3Posts")
                .get("top3", List.class);

        // 캐시가 존재하고, Top3 안에 새로 들어올 가능성이 있으면 캐시 삭제
        if (cachedTop3 != null && CommunityHelper.isInfluenceTop3(cachedTop3, postId, newLikeCount)) {
            cacheManager.getCache("top3Posts").evict("top3");
            log.info("Top 3 게시글에 대한 캐시 Evicted 작업 완료 ");
        }

        return newLikeCount;
    }

    /* LikeCount 총 합산 반환 메서드 */
    @Override
    public Integer getLikeCount( Integer postId) {

        Post post = communityManager.getPost(postId); // Like 버튼을 누를 게시글 객체 반환
        return postLikeRepository.countByPost(post);
    }

    /* Like 수 Top3 게시글 썸네일 리스트 반환 메서드 */
    @Override
    @Cacheable(value = "top3Posts", key = "'top3'")
    public List<TopLikedPosts> getTopLikedPosts() {

        log.info("==> Cache Miss (Top3 게시글 반환) : DB에서 Top3 게시글 정보를 가져옵니다.");
        return communityMapper.findTopLikedPosts(); // like 수로 내림차순 정렬 (동일한 값이 있을 시 date값이 최신인 것이 위로 오게 정렬)
    }

}
