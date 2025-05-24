package bookcalendar.server.Domain.Mypage.Service;

import bookcalendar.server.Domain.Book.Exception.BookException;
import bookcalendar.server.Domain.Community.DTO.Response.PostResponse;
import bookcalendar.server.Domain.Community.Entity.Post;
import bookcalendar.server.Domain.Community.Entity.Scrap;
import bookcalendar.server.Domain.Community.Exception.CommunityException;
import bookcalendar.server.Domain.Community.Manager.CommunityManager;
import bookcalendar.server.Domain.Community.Repository.PostLikeRepository;
import bookcalendar.server.Domain.Community.Repository.PostRepository;
import bookcalendar.server.Domain.Community.Repository.ScrapRepository;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Mypage.DTO.Request.ManualCartRequest;
import bookcalendar.server.Domain.Mypage.DTO.Request.UserInfoEditRequest;
import bookcalendar.server.Domain.Mypage.DTO.Response.*;
import bookcalendar.server.Domain.Mypage.Entity.Cart;
import bookcalendar.server.Domain.Mypage.Manager.MypageManager;
import bookcalendar.server.Domain.Mypage.Repository.CartRepository;
import bookcalendar.server.Domain.Question.Entity.Question;
import bookcalendar.server.Domain.Review.Entity.Review;
import bookcalendar.server.Domain.Review.Repository.ReviewRepository;
import bookcalendar.server.Domain.Review.ReviewException;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MypageServiceImpl implements MypageService {

    private final MypageManager mypageManager;
    private final ReviewRepository reviewRepository;
    private final PostRepository postRepository;
    private final ScrapRepository scrapRepository;
    private final CartRepository cartRepository;
    private final CommunityManager communityManager;
    private final PostLikeRepository postLikeRepository;

    // ======================= User Info Page =========================

    /**
     * 간단한 유저 정보 조회 메서드
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 간단한 유저 정보 DTO (닉네임, 유저 랭크)
     */
    @Override
    public UserSimpleInfoResponse getUserSimpleInfo(CustomUserDetails customUserDetails) {

        // 유저의 정보 객체를 통해 멤버 객체 반환
        Member member = mypageManager.getMember(customUserDetails.getMemberId());

        // 간단한 유저 정보 DTO로 생성하여 반환
        return new UserSimpleInfoResponse(member.getNickName(),member.getRank());
    }

    /**
     * 유저의 모든 정보 조회 메서드
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 유저의 모든 정보 DTO
     */
    @Override
    public UserAllInfoResponse getUserAllInfo(CustomUserDetails customUserDetails) {

        // 유저의 정보 객체를 통해 멤버 객체 반환
        Member member = mypageManager.getMember(customUserDetails.getMemberId());

        // 유저의 모든 정보 DTO로 생성하여 반환
        return UserAllInfoResponse.builder()
                .nickName(member.getNickName())
                .phoneNumber(member.getPhoneNumber())
                .genre(member.getGenre())
                .job(member.getJob())
                .birth(member.getBirth())
                .build();
    }

    /**
     * 내 프로필 수정 API
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param userAllInfoResponse 내 프로필 데이터 수정 요청 DTO
     * @return 수정된 프로필 정보 DTO
     */
    @Override
    @Transactional
    public UserInfoEditResponse updateUserAllInfo(CustomUserDetails customUserDetails, UserInfoEditRequest userAllInfoResponse) {

        // 유저의 정보 객체를 통해 멤버 객체 반환
        Member member = mypageManager.getMember(customUserDetails.getMemberId());

        // 반환된 멤버 엔티티 내부의 수정 메서드 호출
        member.updateProfile(
                userAllInfoResponse.nickName(),
                userAllInfoResponse.phoneNumber(),
                userAllInfoResponse.genre(),
                userAllInfoResponse.job(),
                userAllInfoResponse.birth()
        );

        return UserInfoEditResponse.builder()
                .nickName(member.getNickName())
                .phoneNumber(member.getPhoneNumber())
                .genre(member.getGenre())
                .job(member.getJob())
                .birth(member.getBirth())
                .build();
    }

    // ======================= Review Page =========================

    /**
     * 내 독후감 리스트 일괄 조회 메서드
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 독후감 리스트 반환
     */
    @Override
    @Cacheable(value = "myReviewList", key = "#customUserDetails.memberId")
    public List<MyReviewList> getReviewList(CustomUserDetails customUserDetails) {

        log.info("==> Cache Miss (내 독후감 리스트 반환): DB에서 내 독후감 리스트 정보를 가져옵니다.");

        // 해당 유저가 지금까지 작성한 모든 독후감 객체 반환
        List<Review> myReviewLists = mypageManager.getReviewListByMemberId(customUserDetails.getMemberId());

        // Review → MyReviewList DTO 변환
        return myReviewLists.stream()
                .map(review -> new MyReviewList(
                        review.getReviewId(),
                        review.getBook().getBookName(),
                        review.getDate()
                ))
                .toList();
    }

    /**
     * 내 독후감 상세 조회 메서드
     *
     * @param reviewId 독후감 고유 번호
     * @return 독후감 기록 DTO 반환
     */
    @Override
    public ReviewByReviewIdResponse getReview(Integer reviewId) {

        // 입력받은 review 고유 번호를 통해 독후감 - 질문지 객체 반환
        Review review = mypageManager.getReview(reviewId);
        Question question = mypageManager.getQuestion(reviewId);

        // 독후감 기록 DTO 빌더 패턴으로 반환
        return ReviewByReviewIdResponse.builder()
                .contents(review.getContents())
                .question1(question.getQuestion1())
                .answer1(question.getAnswer1())
                .question2(question.getQuestion2())
                .answer2(question.getAnswer2())
                .question3(question.getQuestion3())
                .answer3(question.getAnswer3())
                .aiResponse(review.getAiResponse())
                .build();

    }

    /**
     * 독후감 삭제 메서드
     *
     * @param reviewId 삭제하고자 하는 독후감 고유 번호
     */
    @Override
    @Transactional
    public void deleteReview(Integer reviewId) {

        // 해당 독후감 고유 번호를 가진 독후감이 있는지 확인
        if(!reviewRepository.existsByReviewId(reviewId)){
            throw new ReviewException(ErrorCode.REVIEW_NOT_FOUND);
        }
        reviewRepository.deleteById(reviewId); // 독후감이 있으면 삭제
    }

    // ======================= Scrap Page =========================

    /**
     * 내 스크랩 리스트 조회 API
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 스크랩 정보 DTO 리스트
     */
    @Override
    public List<MyScrapListResponse> getScrapList(CustomUserDetails customUserDetails) {

        // 유저의 고유 번호를 통해서 스크랩 리스트 반환
        List<Scrap> scrapList = mypageManager.getScrapListByMemberId(customUserDetails.getMemberId());

        // 스크랩 리스트에서 원하는 정보를 DTO로 생성하여 반환
        return scrapList.stream()
                .map(scrap -> new MyScrapListResponse(
                        scrap.getScrapId(),
                        scrap.getPost().getTitle(),
                        scrap.getPost().getMember().getNickName(),
                        scrap.getDate() // scrap date
                ))
                .collect(Collectors.toList());
    }

    /**
     * 스크랩한 게시글 상세 조회 메서드
     *
     * @param scrapId 게시글 고유 번호
     * @return 스크랩 한 게시글 정보 DTO
     */
    @Override
    public PostResponse getScrapDetail(CustomUserDetails customUserDetails,Integer scrapId) {

        Member member = communityManager.getMember(customUserDetails.getMemberId());
        Post post = mypageManager.getPostByScrapId(customUserDetails,scrapId);

        PostResponse postResponse =postRepository.getPostDetail(post.getPostId())
                .orElseThrow(() -> new CommunityException(ErrorCode.POST_NOT_FOUND));

        if(postLikeRepository.existsByPostAndMember(post,member))
            postResponse.setClicked(true);

        // 선택한 게시글 상세 내용 반환
        return postResponse;
    }

    /**
     * 스크랩 취소 메서드
     *
     * @param scrapId 취소하고자 하는 스크랩 고유 번호
     */
    @Override
    @Transactional
    public void deleteScrap(Integer scrapId) {

        // 요청이 들어온 스크랩 객체가 있는지 확인, 없으면 에러 반환
        if(!scrapRepository.existsByScrapId(scrapId)){
            throw new CommunityException(ErrorCode.POST_NOT_FOUND);
        }
        // 있으면 해당 스크랩 객체 취소(삭제)
        Scrap scrap = mypageManager.getScrapByScrapId(scrapId);
        scrapRepository.delete(scrap);
    }

    // ======================= Cart Page =========================

    /**
     * 장바구니에 책 수동 등록 메서드
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param manualCartRequest 등록하고자 하는 책의 정보
     */
    @Override
    @Transactional
    public Cart saveBookToCartByManual(CustomUserDetails customUserDetails, ManualCartRequest manualCartRequest) {

        // 현재의 멤버 객체 반환
        Member member = mypageManager.getMember(customUserDetails.getMemberId());

        // cart 객체 생성
        Cart cart = Cart.builder()
                .bookName(manualCartRequest.bookName())
                .author(manualCartRequest.author())
                .link(manualCartRequest.url())
                .date(LocalDateTime.now())
                .member(member)
                .build();

        return cartRepository.save(cart);
    }

    /**
     * 장바구니 일괄 조회 메서드
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 장바구니 DTO 리스트
     */
    @Override
    public List<Cart> getCartList(CustomUserDetails customUserDetails) {

        // 장바구니의 도서 리스트 반환
        List<Cart> cartList = cartRepository.findByMember_MemberId(customUserDetails.getMemberId());

        return cartList;
    }

    /**
     * 저장된 장바구니 도서 취소 메서드
     *
     * @param cartId 장바구니 객체 고유 번호
     */
    @Override
    @Transactional
    public void deleteCart(Integer cartId) {

        /**
         * 요청한 장바구니 객체가 있는지 확인
         *
         * 1. 거짓 - 도서 없음 오류 반환
         * 2. 참 - Cart DB 테이블에서 삭제
         *
         */
        if(!cartRepository.existsByCartId(cartId)){
            throw new BookException(ErrorCode.BOOK_NOT_FOUND);
        }
        Cart cart = cartRepository.findByCartId(cartId);
        cartRepository.delete(cart);
    }

    // ======================= ETC =========================

    /**
     * 독서 수 & 독후감 작성 수 조회 메서드
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 독서 수 & 독후감 작성 수 DTO
     */
    @Override
    public StatisticResponse getStatistic(CustomUserDetails customUserDetails) {

        // 인증된 유저 정보 객체를 통해 member 객체 반환
        Member member = mypageManager.getMember(customUserDetails.getMemberId());

        return new StatisticResponse(member.getCompletion(), member.getReviewCount());
    }
}
