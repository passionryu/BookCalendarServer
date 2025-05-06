package bookcalendar.server.Domain.Mypage.Repository;

import bookcalendar.server.Domain.Mypage.Entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Integer> {
}
