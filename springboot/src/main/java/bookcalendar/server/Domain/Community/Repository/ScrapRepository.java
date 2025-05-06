package bookcalendar.server.Domain.Community.Repository;

import bookcalendar.server.Domain.Community.Entity.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap,Integer> {

    /**
     * 유저의 고유 번호로 Scrap리스트 반환
     *
     * @param memberId 유저의 고유 번호
     * @return 스크랩 리스트
     */
    List<Scrap> findByMember_MemberId(Integer memberId);

    /**
     * 스크랩 고유 번호를 통한 Scrap 객체 반환
     *
     * @param scrapId 반환하고자 하는 스크랩객체의 고유 번호
     * @return 스크랩 객체
     */
    Optional<Scrap> findByScrapId(Integer scrapId);

    /**
     * 해당 스크랩 고유 번호와 일치하는 스크랩 객체가 존재하는지 확인
     *
     * @param scrapId 스크랩 고유 번호
     * @return 존재 유무
     */
    Boolean existsByScrapId(Integer scrapId);
}
