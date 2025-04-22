package bookcalendar.server.Domain.Question.Service;


import bookcalendar.server.Domain.Book.Entity.Book;
import bookcalendar.server.Domain.Book.Repository.BookRepository;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Exception.MemberException;
import bookcalendar.server.Domain.Member.Repository.MemberRepository;
import bookcalendar.server.Domain.Question.DTO.Request.QuestionAnswerRequest;
import bookcalendar.server.Domain.Question.Entity.Question;
import bookcalendar.server.Domain.Question.Exception.QuestionException;
import bookcalendar.server.Domain.Question.Repository.QuestionRepository;
import bookcalendar.server.Domain.Review.DTO.Response.AiResponse;
import bookcalendar.server.Domain.Review.Entity.Review;
import bookcalendar.server.Domain.Review.Repository.ReviewRepository;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final ChatClient chatClient;
    private final QuestionRepository questionRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;

    /**
     * 질문지 작성 메서드
     *
     * @param customUserDetails
     * @param questionAnswerRequest
     * @return
     */
    @Override
    @Transactional
    public AiResponse writeQuestion(CustomUserDetails customUserDetails, QuestionAnswerRequest questionAnswerRequest) {

        // member 객체 반환
        Member member = memberRepository.findByMemberId(customUserDetails.getMemberId())
                .orElseThrow(()-> new MemberException(ErrorCode.USER_NOT_FOUND));

        // book 객체 반환
        Book book = bookRepository.findByMemberIdAndStatus(customUserDetails.getMemberId(),Book.Status.독서중)
                .orElseThrow(()-> new MemberException(ErrorCode.USER_NOT_FOUND));

        // question 객체 반환
        Question question = questionRepository.findByQuestionId(questionAnswerRequest.questionId())
                .orElseThrow(() -> new QuestionException(ErrorCode.QUESTION_NOT_FOUND));

        // review 객체 반환
        Review review = reviewRepository.findByMember_MemberIdAndDate(member.getMemberId(), LocalDate.now())
                .orElseThrow(()-> new MemberException(ErrorCode.USER_NOT_FOUND));

        // 질문지에 대한 답변 저장 메서드
        saveAnswer(question,questionAnswerRequest);

        // 질문지에 대한 유저의 피드백 저장 메서드
        saveFeedback(question,questionAnswerRequest);

        // 남은 날짜 계산 메서드
        Integer remainDate = getDaysUntilFinish(book);

        // 하루 평균 독서 페이지 계산 메서드
        Integer averagePage = (book.getTotalPage()-review.getPages()) / remainDate;

        // 남은 기간 몇 page씩 읽어야 하는지 알려주는 문자열
        String averageMessage = member.getNickName()+"님은 남은 기간 동안 하루에 "
                + averagePage + "page씩 읽으면 목표를 달성할 수 있습니다.";

        // aiMessage 반환을 위한 프롬프트 메시지 생성 메서드
        String aiMessagePrompt = aiMessagePromptMaker(review,question);

        // aiMessage 반환 메서드
        String aiMessage = chatClient.call(aiMessagePrompt);

        // aiMessage review 객체에 저장
        review.setAiResponse(aiMessage);

        // AiResponse 객체를 생성 후 반환
        AiResponse aiResponse =  AiResponse.builder()
                .totalPages(book.getTotalPage())
                .currentPages(review.getPages())
                .progress(review.getProgress())
                .finishDate(book.getFinishDate())
                .remainDate(remainDate)
                .averageMessage(averageMessage)
                .aiMessage(aiMessage)
                .build();

        return aiResponse;
    }

    // ======================= Helper Code =========================

    /**
     * 질문지에 대한 답변 저장 메서드
     *
     * @param question
     * @param questionAnswerRequest
     */
    private void saveAnswer(Question question,QuestionAnswerRequest questionAnswerRequest) {
        if (questionAnswerRequest.answer1() != null)
            question.setAnswer1(questionAnswerRequest.answer1());
        if (questionAnswerRequest.answer2() != null)
            question.setAnswer2(questionAnswerRequest.answer2());
        if (questionAnswerRequest.answer3() != null)
            question.setAnswer3(questionAnswerRequest.answer3());
    }

    /**
     * 질문지에 대한 유저의 피드백 저장 메서드
     *
     * @param question
     * @param questionAnswerRequest
     */
    private void saveFeedback(Question question,QuestionAnswerRequest questionAnswerRequest) {

        if (question.getFeedback1() == 0 && questionAnswerRequest.feedback1() != 0)
            question.setFeedback1(questionAnswerRequest.feedback1());
        if (question.getFeedback2() == 0 && questionAnswerRequest.feedback2() != 0)
            question.setFeedback2(questionAnswerRequest.feedback2());
        if (question.getFeedback3() == 0 && questionAnswerRequest.feedback3() != 0)
            question.setFeedback3(questionAnswerRequest.feedback3());
    }

    /**
     * 남은 날짜 계산 로직
     *
     * @param book
     * @return
     */
    private Integer getDaysUntilFinish(Book book) {
        return Math.toIntExact(ChronoUnit.DAYS.between(LocalDate.now(), book.getFinishDate()));
    }

    /**
     * aiMessage 반환을 위한 프롬프트 메시지 반환 메서드
     *
     * @param review
     * @param question
     * @return
     */
    private String aiMessagePromptMaker(Review review, Question question) {

        return  "너는 지금 이 어플리케이션 사용자가 작성한 독후감 그리고 3가지 질문지에 대한 답변을 읽고 15~20줄 정도의 격려 및 평가 메시지를 반환하는 AI이다." +
                "다음 이어지는 내용들은 유저의 독후감과 질문지와  질문지에 대한 답변들이다." +
                "[유저의 독후감] : " + review.getContents() +
                "[1번 질문] : " + question.getQuestion1() +
                "[1번 답변] : " + question.getAnswer1() +
                "[2번 질문] : " + question.getQuestion2() +
                "[2번 답변] : " + question.getAnswer2() +
                "[3번 질문] : " + question.getQuestion3() +
                "[3번 답변] : " + question.getAnswer3();
    }
}
