package bookcalendar.server.Domain.Member.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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
    private Long memberId;

    @Column(nullable = false, unique = true, length = 50)
    private String nickName;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false)
    private LocalDate birth;

    @Column(nullable = false, unique = true, length = 20)
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
     * 유저의 랭킹 (예 : 상위 50%)
     * Default = 100 으로 시작
     * rank는 sql 예약어 이므로 백틱으로 감쌈
     */
    @Column(name = "`rank`")
    @org.hibernate.annotations.ColumnDefault("100")
    private Integer rank;

    @Column(nullable = false, length = 20)
    private String role;
}
