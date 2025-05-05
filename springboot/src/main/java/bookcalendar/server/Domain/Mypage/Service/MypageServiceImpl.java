package bookcalendar.server.Domain.Mypage.Service;

import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Mypage.DTO.Response.UserSimpleInfoResponse;
import bookcalendar.server.Domain.Mypage.Manager.MypageManager;
import bookcalendar.server.global.Security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
}
