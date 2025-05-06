package bookcalendar.server.Domain.Mypage.Service;

import bookcalendar.server.Domain.Mypage.DTO.Request.UserInfoEditRequest;
import bookcalendar.server.Domain.Mypage.DTO.Response.*;
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
}
