package bookcalendar.server.Domain.Member.Entity;

import bookcalendar.server.Domain.Book.Entity.Book;
import bookcalendar.server.Domain.Community.Entity.Post;
import bookcalendar.server.Domain.Community.Entity.Scrap;
import bookcalendar.server.Domain.Member.DTO.Request.RegisterRequest;
import bookcalendar.server.Domain.Mypage.Entity.Cart;
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

    @Column(name = "registerDate")
    private LocalDate registerDate;

    // ======================= 테이블 관계 정의 영역 =========================

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

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Scrap> scraps = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Cart> carts = new ArrayList<>();

    // ======================= 메서드 영역 =========================

    /**
     * 내 프로필 수정 메서드
     *
     * @param nickName 닉네임
     * @param phoneNumber 전화번호
     * @param genre 좋아하는 장르
     * @param job 직업
     * @param birth 생년월일
     *
     * null을 사용하여, 일부만 요청이 들어와도 안전하게 반영
     */
    public void updateProfile(String nickName, String phoneNumber, String genre, String job, LocalDate birth) {
        if (nickName != null) this.nickName = nickName;
        if (phoneNumber != null) this.phoneNumber = phoneNumber;
        if (genre != null) this.genre = genre;
        if (job != null) this.job = job;
        if (birth != null) this.birth = birth;
    }

    /**
     * 멤버 엔티티 생성 메서드
     *
     * @param registerRequest 회원가입 요청 DTO
     * @param encodedPassword 암호화된 비밀번호
     * @return 멤버 객체
     */
    public static Member createMember(RegisterRequest registerRequest, String encodedPassword) {
        return Member.builder()
                .nickName(registerRequest.nickName())
                .password(encodedPassword)
                .birth(registerRequest.birth())
                .phoneNumber(registerRequest.phoneNumber())
                .genre(registerRequest.genre())
                .job(registerRequest.job())
                .completion(0)
                .reviewCount(0)
                .rank(100)
                .role("USER")
                .registerDate(LocalDate.now())
                .build();
    }
}
