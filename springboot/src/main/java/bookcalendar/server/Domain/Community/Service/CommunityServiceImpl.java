package bookcalendar.server.Domain.Community.Service;

import bookcalendar.server.Domain.Book.Exception.BookException;
import bookcalendar.server.Domain.Community.DTO.Request.CommentRequest;
import bookcalendar.server.Domain.Community.DTO.Request.PostRequest;
import bookcalendar.server.Domain.Community.DTO.Response.CommentResponse;
import bookcalendar.server.Domain.Community.DTO.Response.PostListResponse;
import bookcalendar.server.Domain.Community.DTO.Response.PostResponse;
import bookcalendar.server.Domain.Community.Entity.Comment;
import bookcalendar.server.Domain.Community.Entity.Post;
import bookcalendar.server.Domain.Community.Entity.Scrap;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

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

    @Override
    @Transactional
    public Integer writePost(CustomUserDetails customUserDetails, PostRequest postRequest) {

        // 현재 멤버 객체 반환
        Member member = communityManager.getMember(customUserDetails.getMemberId());

        // 입력 정보 및 유저 정보를 통해 Post 엔티티 생성
        Post post = postRepository.save(CommunityHelper.postEntityBuilder(member, postRequest));
        return post.getPostId();
    }

    @Override
    public void deletePost(CustomUserDetails customUserDetails, Integer postId) {

        // 게시글 ID를 통한 게시글 객체 반환
        Post post = communityManager.getPost(postId);

        // 현재 유저가 삭제하려는 게시글에 삭제 권한이 있는지 확인
        CommunityHelper.checkOwnership_post(customUserDetails, post);

        // 해당 post 객체 삭제
        postRepository.delete(post);
    }

    /* description : 독서 완료 시 캐싱 데이터 무효화 */
    /* description : 랭킹 변동 시 캐싱 데이터 무효화 */
    @Override
    @Cacheable(value = "rankCache", key = "#customUserDetails.memberId")
    public RankResponse getRank(CustomUserDetails customUserDetails) {

        // 인증된 유저 객체의를 활용한 Member 객체 반환
        Member member = communityManager.getMember(customUserDetails.getMemberId());

        // 유저 메달 정보 & 랭킹 반환
        return new RankResponse(member.getNickName(), member.getRank(), member.getReviewCount());
    }

    /* todo : 캐싱 시스템 적용하기 - 누군가 올리면 캐싱 무효화 */
    @Override
    public List<PostListResponse> getPostList() {

        // 게시글 리스트 DTO 반환
        return postRepository.findAllPostSummaries();
    }

    @Override
    public PostResponse getPostDetail(Integer postId) {

        // 선택한 게시글 상세 내용 반환
        return postRepository.getPostDetail(postId)
                .orElseThrow(() -> new CommunityException(ErrorCode.POST_NOT_FOUND));
    }

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

    @Override
    public List<CommentResponse> getCommentList(Integer postId) {

        // 게시글에서 댓글 리스트 반환 매퍼 호출
        return communityMapper.getCommentsByPostId(postId);
    }

    @Override
    public void deleteComment(CustomUserDetails customUserDetails, Integer commentId) {

        Comment comment = communityManager.getComment(commentId);

        CommunityHelper.checkOwnership_comment(customUserDetails, comment); // 현재 유저가 삭제하려는 댓글에 삭제 권한이 있는지 확인
        commentRepository.delete(comment);
    }

    @Override
    public void deleteCommentByPostOwner(CustomUserDetails customUserDetails, Integer postId, Integer commentId) {

        Comment comment = communityManager.getComment(commentId);
        Post post = communityManager.getPost(comment.getPost().getPostId());

        CommunityHelper.checkOwnership_post(customUserDetails, post); // 위 게시글의 작성자가 현재 유저와 동일한지 검증 - 삭제 권한 확인
        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public void reportPost(CustomUserDetails customUserDetails, Integer postId) {

        Post post = communityManager.getPost(postId);

        if(postReportRepository.existsByPostAndMember(post, communityManager.getMember(customUserDetails.getMemberId()))){
         throw new CommunityException(ErrorCode.ALREADY_REPORT_POST);
        }
        post.increaseReportCount(); // 도메인 객체에서 신고 수 1 증가
    }

    @Override
    @Transactional
    public void reportComment(CustomUserDetails customUserDetails, Integer commentId) {

        Comment comment = communityManager.getComment(commentId);

        if(commentReportRepository.existsByCommentAndMember(comment, communityManager.getMember(customUserDetails.getMemberId()))){
            throw new CommunityException(ErrorCode.ALREADY_REPORT_COMMENT);
        }
        comment.increaseReportCount(); // 도메인 객체에서 신고수 1 증가
    }

    @Override
    @Transactional
    public void scrapPost(CustomUserDetails customUserDetails, Integer postId) {

        Member member = communityManager.getMember(customUserDetails.getMemberId());
        Post post = communityManager.getPost(postId);

        if(scrapRepository.existByMember_MemberIdAndPost_PostId(member.getMemberId(), post.getPostId())){
            throw new CommunityException(ErrorCode.ALREADY_SCRAP);
        }

        Scrap scrap = CommunityHelper.scrapEntityBuilder(member, post); // Helper 클래스에서 스크랩 객체 생성 레이어 호출
        scrapRepository.save(scrap); // Scrap 객체 저장
    }

    @Override
    public List<PostListResponse> searchPost(String keyword) {
        return postRepository.searchPostsByKeyword(keyword);
    }
}
