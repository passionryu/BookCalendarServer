package bookcalendar.server.Domain.Mypage.Entity;

import bookcalendar.server.Domain.Member.Entity.Member;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "cart")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cartId")
    private Integer cartId;

    @Column(name = "bookName", nullable = false, length = 255)
    private String bookName;

    @Column(name = "author", length = 255)
    private String author;

    @Column(name = "link", length = 255)
    private String link;

    @Column(name = "date", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime date;

    /**
     * 다대일 관계: Cart → Member
     * Member가 삭제되면 Cart도 함께 삭제되도록 설정 (DB에선 ON DELETE CASCADE)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId", nullable = false)
    @JsonIgnore
    private Member member;
}