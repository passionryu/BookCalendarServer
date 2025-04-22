package bookcalendar.server.Domain.Review.Entity;


import bookcalendar.server.Domain.Book.Entity.Book;
import bookcalendar.server.Domain.Member.Entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "review", indexes = {
        @Index(name = "idx_review_date_memberId", columnList = "date, member_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewId;

    @Lob
    @Column(nullable = false)
    private String contents;

    @Lob
    private String aiResponse;

    private Integer progress;

    private Integer pages;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId", nullable = false, foreignKey = @ForeignKey(name = "fk_review_member"))
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookId", nullable = false, foreignKey = @ForeignKey(name = "fk_review_book"))
    private Book book;

    @Column(name = "date", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDate date;
}
