package bookcalendar.server.Domain.Review.Repository;

import bookcalendar.server.Domain.Review.Entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    /**
     * memberId와 날짜를 통해 review 객체를 반환
     *
     * @param memberId
     * @param date
     * @return
     */
    Optional<Review> findByMember_MemberIdAndDate(Integer memberId, LocalDate date);

    /**
     * memberId & bookId로 review List 반환
     *
     * @param memberId
     * @param bookId
     * @return
     */
    List<Review> findByMember_MemberIdAndBook_BookId(Integer memberId, Integer bookId);

}
