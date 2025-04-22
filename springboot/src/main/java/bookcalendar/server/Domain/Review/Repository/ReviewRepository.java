package bookcalendar.server.Domain.Review.Repository;

import bookcalendar.server.Domain.Review.Entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository  extends JpaRepository<Review, Integer> {

}
