package bookcalendar.server.Domain.Question.Manager;

import bookcalendar.server.Domain.Book.Entity.Book;
import bookcalendar.server.Domain.Book.Repository.BookRepository;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Exception.MemberException;
import bookcalendar.server.Domain.Member.Repository.MemberRepository;
import bookcalendar.server.Domain.Question.Entity.Question;
import bookcalendar.server.Domain.Question.Exception.QuestionException;
import bookcalendar.server.Domain.Question.Helper.QuestionHelper;
import bookcalendar.server.Domain.Question.Repository.QuestionRepository;
import bookcalendar.server.Domain.Review.Entity.Review;
import bookcalendar.server.Domain.Review.Repository.ReviewRepository;
import bookcalendar.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.ChatClient;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class QuestionManager {

    private final ChatClient chatClient;
    private final QuestionRepository questionRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;

    public Member getMember(Integer memberId) {
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));
    }

    public Book getCurrentReadingBook(Integer memberId) {
        return bookRepository.findByMemberIdAndStatus(memberId, Book.Status.독서중)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));
    }

    public Question getQuestion(Integer questionId) {
        return questionRepository.findByQuestionId(questionId)
                .orElseThrow(() -> new QuestionException(ErrorCode.QUESTION_NOT_FOUND));
    }

    public Review getTodayReview(Integer memberId) {
        return reviewRepository.findByMember_MemberIdAndDate(memberId, LocalDate.now())
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));
    }

    public String generateAiMessage(Review review, Question question) {
        String prompt = QuestionHelper.aiMessagePromptMaker(review, question);
        return chatClient.call(prompt);
    }
}
