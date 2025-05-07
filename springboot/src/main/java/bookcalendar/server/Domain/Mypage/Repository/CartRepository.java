package bookcalendar.server.Domain.Mypage.Repository;

import bookcalendar.server.Domain.Mypage.Entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Integer> {

    /**
     * 유저의 고유 번호를 통해 해당 유저의 모든 장바구니 반환
     *
     * @param memberId
     * @return
     */
    List<Cart> findByMember_MemberId(Integer memberId);
}
