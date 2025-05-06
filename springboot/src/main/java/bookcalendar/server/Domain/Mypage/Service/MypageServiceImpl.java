package bookcalendar.server.Domain.Mypage.Service;

import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Mypage.DTO.Request.UserInfoEditRequest;
import bookcalendar.server.Domain.Mypage.DTO.Response.UserAllInfoResponse;
import bookcalendar.server.Domain.Mypage.DTO.Response.UserInfoEditResponse;
import bookcalendar.server.Domain.Mypage.DTO.Response.UserSimpleInfoResponse;
import bookcalendar.server.Domain.Mypage.Manager.MypageManager;
import bookcalendar.server.global.Security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MypageServiceImpl implements MypageService {

    private final MypageManager mypageManager;

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
}
