package bookcalendar.server.Domain.Community.Entity;

import bookcalendar.server.Domain.Member.Entity.Member;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer postId;

    @Column(nullable = false, length = 255)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId", nullable = false)
    @JsonManagedReference // 직렬화 시 Member는 포함하되, 역방향 참조는 제한
    private Member member;

    @Column(columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime date;

    @Lob
    @Column(nullable = false)
    private String contents;

//    @Column
//    @org.hibernate.annotations.ColumnDefault("0")
//    private Integer reportCount;

    @Builder.Default
    @Column
    private Integer reportCount = 0;

    // 게시글 신고 기능 사용시 호출 되는 메서드
    public void increaseReportCount() {
        this.reportCount += 1;
    }

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> postLikes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Scrap> scraps = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostReport> postReports = new ArrayList<>();


    @PrePersist
    protected void onCreate() {
        if (this.date == null) this.date = LocalDateTime.now();
    }


}
