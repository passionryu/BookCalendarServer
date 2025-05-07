package bookcalendar.server.Domain.Mypage.Service;

import bookcalendar.server.Domain.Community.DTO.Response.PostResponse;
import bookcalendar.server.Domain.Mypage.DTO.Request.ManualCartRequest;
import bookcalendar.server.Domain.Mypage.DTO.Request.UserInfoEditRequest;
import bookcalendar.server.Domain.Mypage.DTO.Response.*;
import bookcalendar.server.Domain.Mypage.Entity.Cart;
import bookcalendar.server.global.Security.CustomUserDetails;

import java.util.List;

public interface MypageService {

    /**
     * 간단한 유저 정보 조회 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 간단한 유저 정보 DTO (닉네임, 유저 랭크)
     */
    UserSimpleInfoResponse getUserSimpleInfo(CustomUserDetails customUserDetails);

    /**
     * 유저의 모든 정보 조회 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 유저의 모든 정보 DTO
     */
    UserAllInfoResponse getUserAllInfo(CustomUserDetails customUserDetails);

    /**
     * 내 프로필 수정 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param userAllInfoResponse 내 프로필 데이터 수정 요청 DTO
     * @return 수정된 프로필 정보 DTO
     */
    UserInfoEditResponse updateUserAllInfo(CustomUserDetails customUserDetails, UserInfoEditRequest userAllInfoResponse);

    /**
     * 내 독후감 리스트 일괄 조회 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 독후감 리스트 반환
     */
    List<MyReviewList> getReviewList(CustomUserDetails customUserDetails);

    /**
     * 내 독후감 상세 조회 인터페이스
     *
     * @param reviewId 독후감 고유 번호
     * @return 독후감 기록 DTO 반호나
     */
    ReviewByReviewIdResponse getReview(Integer reviewId);

    /**
     * 독후감 삭제 인터페이스
     *
     * @param reviewId 삭제하고자 하는 독후감의 고유 번호
     */
    void deleteReview(Integer reviewId);

    /**
     * 내 스크랩 리스트 반환 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 스크랩한 게시글 리스트 반환
     */
    List<MyScrapListResponse> getScrapList(CustomUserDetails customUserDetails);

    /**
     * 스크랩 한 게시글 반환 인터페이스
     *
     * @param scrapId 게시글 고유 번호
     * @return 게시글 정보
     */
    PostResponse getScrapDetail(Integer scrapId);

    /**
     * 스크랩 취소 인터페이스
     *
     * @param scrapId 취소하고자 하는 스크랩 고유 번호
     */
    void deleteScrap(Integer scrapId);

    /**
     * 장바구니에 책 수동 등록 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param manualCartRequest 등록하고자 하는 책의 정보
     */
    Cart saveBookToCartByManual(CustomUserDetails customUserDetails, ManualCartRequest manualCartRequest);

    /**
     * 장바구니 일괄 조회 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 장바구니 DTO 리스트
     */
    List<Cart> getCartList(CustomUserDetails customUserDetails);

    /**
     * 저장된 장바구니 도서 취소 인터페이스
     *
     * @param cartId 장바구니 객체 고유 번호
     */
    void deleteCart(Integer cartId);

    /**
     * 독서 수 & 독후감 작성 수 조회 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return 독서 수 & 독후감 작성 수 DTO
     */
    StatisticResponse getStatistic(CustomUserDetails customUserDetails);

}
