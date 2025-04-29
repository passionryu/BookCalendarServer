package bookcalendar.server.Domain.Member.Entity;

import bookcalendar.server.Domain.Book.Entity.Book;
import bookcalendar.server.Domain.Community.Entity.Post;
import bookcalendar.server.Domain.Review.Entity.Review;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "memberId")
    private Integer memberId;

    @Column(nullable = false, unique = true, length = 50)
    private String nickName;

    @Column(nullable = false, length = 255)
    @JsonIgnore
    private String password;

    @Column(nullable = false)
    private LocalDate birth;

    @Column(nullable = false, unique = true, length = 20)
    @JsonIgnore
    private String phoneNumber;

    @Column(length = 100)
    private String genre;

    @Column(length = 50)
    private String job;

    /**
     * 유저의 독서 권수
     * Default = 0 으로 시작
     */
    @Column
    @org.hibernate.annotations.ColumnDefault("0")
    private Integer completion;

    /**
     * 유저가 작성한 리뷰 수
     * Default = 0 으로 시작
     */
    @Column
    @org.hibernate.annotations.ColumnDefault("0")
    private Integer reviewCount;

    /**
     * 유저의 랭킹 (예 : 상위 50%)
     * Default = 100 으로 시작
     * rank는 sql 예약어 이므로 백틱으로 감쌈
     */
    @Column(name = "`rank`")
    @org.hibernate.annotations.ColumnDefault("100")
    private Integer rank;

    @Column(nullable = false, length = 20)
    private String role;

    /**
     * Member와 Book 간의 일대다 관계
     * mappedBy: Book 엔티티의 member 필드가 관계의 주인임을 나타냄
     * cascade: Member 삭제 시 연관된 Book도 삭제 (DB의 ON DELETE CASCADE와 일치)
     */
    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Book 리스트를 JSON 직렬화에서 제외
    private List<Book> books = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Review> reviews = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Post 리스트를 JSON 직렬화에서 제외
    private List<Post> posts = new ArrayList<>();

}
