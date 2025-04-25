package bookcalendar.server.Domain.Book.Repository;

import bookcalendar.server.Domain.Book.Entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer> {

    /**
     * 현재 독서중인 도서가 있는지 확인
     *
     * @param memberId 유저의 고유 번호
     * @param status "독서중"인 도서
     * @return True False
     */
    boolean existsByMemberIdAndStatus(Integer memberId, Book.Status status);

    /**
     * 현재 독서중인 도서 반환
     *
     * @param memberId 유저의 고유 번호
     * @param status "독서중"인 도서
     * @return 현재 유저가 독서중인 도서
     */
    Optional<Book> findByMemberIdAndStatus(Integer memberId, Book.Status status);

    @Query("SELECT b FROM Book b WHERE b.memberId = :memberId AND " +
            "b.startDate <= :endOfMonth AND b.finishDate >= :startOfMonth")
    List<Book> findBooksInMonth(@Param("memberId") Integer memberId,
                                @Param("startOfMonth") LocalDate startOfMonth,
                                @Param("endOfMonth") LocalDate endOfMonth);

}
