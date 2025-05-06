package bookcalendar.server.Domain.Community.Repository;

import bookcalendar.server.Domain.Community.Entity.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScrapRepository extends JpaRepository<Scrap,Integer> {

    /**
     * 유저의 고유 번호로 Scrap리스트 반환
     *
     * @param memberId 유저의 고유 번호
     * @return 스크랩 리스트
     */
    List<Scrap> findByMember_MemberId(Integer memberId);
}
