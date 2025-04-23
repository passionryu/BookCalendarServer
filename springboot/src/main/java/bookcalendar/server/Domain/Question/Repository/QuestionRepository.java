package bookcalendar.server.Domain.Question.Repository;

import bookcalendar.server.Domain.Question.Entity.Question;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Integer> {

    /**
     * questionId를 통한 question객체 반환
     *
     * @param questionId
     * @return
     */
    Optional<Question> findByQuestionId(Integer questionId);

    /**
     * reviewId로 Question 조회
     *
     * @param reviewId
     * @return
     */
    Optional<Question> findByReview_ReviewId(Integer reviewId);
}
