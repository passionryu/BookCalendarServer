package bookcalendar.server.Domain.Question.Service;

import bookcalendar.server.Domain.Book.Entity.Book;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Question.DTO.Request.QuestionAnswerRequest;
import bookcalendar.server.Domain.Question.Entity.Question;
import bookcalendar.server.Domain.Question.Helper.QuestionHelper;
import bookcalendar.server.Domain.Question.Manager.QuestionManager;
import bookcalendar.server.Domain.Review.DTO.Response.AiResponse;
import bookcalendar.server.Domain.Review.Entity.Review;
import bookcalendar.server.global.Security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionManager questionManager;

    /**
     * 질문지에 대한 답변 작성 메서드
     *
     * @param user
     * @param request
     * @return
     */
    @Override
    @Transactional
    public AiResponse writeQuestion(CustomUserDetails user, QuestionAnswerRequest request) {

        // Member, Book, Question, Review 객체 반환
        Member member = questionManager.getMember(user.getMemberId());
        Book book = questionManager.getCurrentReadingBook(user.getMemberId());
        Question question = questionManager.getQuestion(request.questionId());
        Review review = questionManager.getTodayReview(user.getMemberId());

        // AI 질문지에 대한 답변과 피드백 결과 저장
        QuestionHelper.handleQuestionAnswer(question, request);
        QuestionHelper.handleQuestionFeedback(question, request);

        // 독서완료까지 남은 날짜 계산
        int remainDate = QuestionHelper.calculateRemainDate(book);

        // 하루에 몇 페이지씩 읽어야 하는지 계산
        int averagePages = QuestionHelper.calculateAveragePages(book, review, remainDate);

        // AI 사서의 피드백 저장
        String aiMessage = questionManager.generateAiMessage(review, question);
        review.setAiResponse(aiMessage);

        return QuestionHelper.buildAiResponse(book, review, member, remainDate, averagePages, aiMessage);
    }

}
