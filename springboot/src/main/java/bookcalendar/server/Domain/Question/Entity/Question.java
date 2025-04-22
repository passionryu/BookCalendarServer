package bookcalendar.server.Domain.Question.Entity;

import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Review.Entity.Review;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "question", indexes = {
        @Index(name = "idx_reviewId", columnList = "review_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewId", foreignKey = @ForeignKey(name = "fk_question_review"), nullable = true)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId", foreignKey = @ForeignKey(name = "fk_question_member"), nullable = false)
    private Member member;

    @Column(nullable = false)
    private String question1;

    @Column(nullable = false)
    private String question2;

    @Column(nullable = false)
    private String question3;

    @Column(columnDefinition = "TEXT")
    private String answer1;

    @Column(columnDefinition = "TEXT")
    private String answer2;

    @Column(columnDefinition = "TEXT")
    private String answer3;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer feedback1;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer feedback2;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer feedback3;
}