package bookcalendar.server.Domain.Book.Entity;

import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Review.Entity.Review;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

@Entity
@Table(name = "book")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookId")
    private Integer bookId;

    @Column(name = "bookName", nullable = false, length = 255)
    private String bookName;

    @Column(name = "author", nullable = false, length = 100)
    private String author;

    @Column(name = "totalPage", nullable = false)
    private Integer totalPage;

    @Column(name = "genre", length = 100)
    private String genre;

    //@Column(name = "memberId")
    //private Integer memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @org.hibernate.annotations.ColumnDefault("독서중")
    private Status status;

    @Column(name = "startDate", nullable = false)
    private LocalDate startDate;

    @Column(name = "finishDate", nullable = false)
    private LocalDate finishDate;

    @ManyToOne
    @JoinColumn(name = "memberId", referencedColumnName = "memberId", insertable = false, updatable = false)
    private Member member;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    /**
     * 독서의 상태
     * 1. 독서 중(기본값)
     * 2. 독서 완료
     * 3. 독서 포기 (해당 도서에 대한 데이터는 삭제 X)
     */
    public enum Status {
        독서중, 독서_완료, 독서_포기
    }
}