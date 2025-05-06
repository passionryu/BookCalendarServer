package bookcalendar.server.Domain.Review.Repository;

import bookcalendar.server.Domain.Review.Entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
     * memberId와 날짜를 통해 이날에 독후감이 있는지 없는지 반환
     *
     * @param memberId
     * @param date
     * @return
     */
    boolean existsByMember_MemberIdAndDate(Integer memberId, LocalDate date);

    /**
     * memberId & bookId로 review List 반환
     *
     * @param memberId
     * @param bookId
     * @return
     */
    List<Review> findByMember_MemberIdAndBook_BookId(Integer memberId, Integer bookId);

    /**
     * BookId로 review 객체 반환
     *
     * @param bookId
     * @return
     */
    List<Review> findByBook_BookId(Integer bookId);

    @Query("SELECT r FROM Review r " +
            "WHERE r.member.memberId = :memberId " +
            "AND YEAR(r.date) = :year " +
            "AND MONTH(r.date) = :month")
    List<Review> findByMemberIdAndMonth(
            @Param("memberId") Integer memberId,
            @Param("year") int year,
            @Param("month") int month);

    /**
     * 유저의 고유 번호를 통해 모든 독후감 리스트 반환
     *
     * @param memberId 유저의 고유 번호
     * @return 록후감 리스트 반환
     */
    List<Review> findByMember_MemberId(Integer memberId);

    /**
     * 독후감 고유 번호를 통해 독후감 반환
     *
     * @param reviewId 독후감 고유 번호
     * @return 독후감 객체
     */
    Optional<Review> findByReviewId(Integer reviewId);
}
