package bookcalendar.server.Domain.Member.Service;

import bookcalendar.server.Domain.Member.DTO.Request.RegisterRequest;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Exception.MemberException;
import bookcalendar.server.Domain.Member.Repository.MemberRepository;
import bookcalendar.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    // ======================= 회원가입 로직 =========================

    /**
     * 회원가입 메서드
     *
     * @param registerRequest 회원가입 데이터
     * @return member 객체
     *
     * @see #nicknameExists(String)
     * @see #phoneNumberExists(String)
     */
    @Override
    public Member register(RegisterRequest registerRequest) {

        // 닉네임 중복 체크 로직
        if (nicknameExists(registerRequest.nickName())) {
            throw new MemberException(ErrorCode.ALREADY_EXIST_NICKNAME);
        }

        // 전화번호 중복 체크 로직
        if (phoneNumberExists(registerRequest.phoneNumber())) {
            throw new MemberException(ErrorCode.ALREADY_EXIST_PHONE_NUMBER);
        }

        // 비밀번호 해싱
        String encodedPassword = passwordEncoder.encode(registerRequest.password());

        // Member 엔티티 생성
        Member member = Member.builder()
                .nickName(registerRequest.nickName())
                .password(encodedPassword)
                .birth(registerRequest.birth())
                .phoneNumber(registerRequest.phoneNumber())
                .genre(registerRequest.genre())
                .job(registerRequest.job())
                .completion(0)
                .rank(100)
                .role("USER")
                .build();

        // Member 엔티티 DB 저장 후 반환
        return memberRepository.save(member);
    }

    /**
     * 닉네임 중복 체크 메서드
     *
     * @param nickname 회원가입 요청이 들어온 닉네임
     * @return Ture / False
     */
    private boolean nicknameExists(String nickname) {
        return memberRepository.existsByNickName(nickname);
    }

    /**
     * 전화번호 중복 체크 메서드
     *
     * @param phoneNumber 회원가입 요청이 들어온 전화번호
     * @return Ture / False
     */
    private boolean phoneNumberExists(String phoneNumber) {
        return memberRepository.existsByPhoneNumber(phoneNumber);
    }

    // ======================= 로그인 로직 =========================

    /**
     * 로그인 시 비밀번호 검증 메서드
     *
     * @param password
     * @param storedPassword
     * @return
     */
    public boolean validateLogin(String password, String storedPassword) {
        // 로그인 시 입력된 비밀번호와 DB에 저장된 해시된 비밀번호 비교
        return passwordEncoder.matches(password, storedPassword);
    }
}
