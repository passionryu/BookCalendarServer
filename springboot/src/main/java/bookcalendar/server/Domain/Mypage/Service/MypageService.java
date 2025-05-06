package bookcalendar.server.Domain.Mypage.Service;

import bookcalendar.server.Domain.Mypage.DTO.Response.UserAllInfoResponse;
import bookcalendar.server.Domain.Mypage.DTO.Response.UserSimpleInfoResponse;
import bookcalendar.server.global.Security.CustomUserDetails;

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


}
