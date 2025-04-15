package bookcalendar.server.Domain.Member.Repository;

import bookcalendar.server.Domain.Member.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * 닉네임 조회 메서드
     *
     * @param nickName 조회할 사용자 닉네임
     * @return 해당 닉네임을 가진 사용자가 존재하면 true, 없으면 false
     */
    boolean existsByNickName(String nickName);

    /**
     * 전화번호 존재 여부 확인 메서드
     *
     * @param phoneNumber
     * @return
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * 닉네임으로 유저 객체 조회
     *
     * @param nickName 로그인 요청한 닉네임
     * @return 유저 객체
     */
    Optional<Member> findByNickName(String nickName);

}
