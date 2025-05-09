package bookcalendar.server.Domain.Community.Entity;

import bookcalendar.server.Domain.Member.Entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postId", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId", nullable = false)
    private Member member;

    @Lob
    @Column(nullable = false)
    private String contents;

    @Column(columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime date;

//    @Column
//    @org.hibernate.annotations.ColumnDefault("0")
//    private Integer reportCount;

    @Builder.Default
    @Column
    private Integer reportCount = 0;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CommentReport> commentReports = new ArrayList<>();


    // 댓글 신고 기능 사용시 호출 되는 메서드
    public void increaseReportCount() {
        this.reportCount += 1;
    }

    @PrePersist
    protected void onCreate() {
        if (this.date == null) this.date = LocalDateTime.now();
    }
}
