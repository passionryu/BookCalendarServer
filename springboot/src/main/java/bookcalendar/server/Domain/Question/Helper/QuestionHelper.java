package bookcalendar.server.Domain.Question.Helper;

import bookcalendar.server.Domain.Book.Entity.Book;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Question.DTO.Request.QuestionAnswerRequest;
import bookcalendar.server.Domain.Question.Entity.Question;
import bookcalendar.server.Domain.Review.DTO.Response.AiResponse;
import bookcalendar.server.Domain.Review.Entity.Review;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class QuestionHelper{

    /**
     * 질문지에 대한 답변 저장 메서드
     *
     * @param question
     * @param questionAnswerRequest
     */
    public static void saveAnswer(Question question, QuestionAnswerRequest questionAnswerRequest) {
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
    public static void saveFeedback(Question question,QuestionAnswerRequest questionAnswerRequest) {

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
    public static Integer getDaysUntilFinish(Book book) {
        return Math.toIntExact(ChronoUnit.DAYS.between(LocalDate.now(), book.getFinishDate()));
    }

    /**
     * aiMessage 반환을 위한 프롬프트 메시지 반환 메서드
     *
     * @param review
     * @param question
     * @return
     */
    public static String aiMessagePromptMaker(Review review, Question question) {

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

    /**
     * 질문 저장 메서드
     *
     * @param question 질문 객체
     * @param request
     */
    public static void handleQuestionAnswer(Question question, QuestionAnswerRequest request) {
        QuestionHelper.saveAnswer(question, request);
    }

    /**
     * 피드백 저장 메서드
     *
     * @param question 질문 객체
     * @param request
     */
    public static void handleQuestionFeedback(Question question, QuestionAnswerRequest request) {
        QuestionHelper.saveFeedback(question, request);
    }

    /**
     * 독서 완료까지 남은 날자 계산 함수
     *
     * @param book 현재 독서 중인 도서 객체
     * @return 남은 날짜 반환
     */
    public static int calculateRemainDate(Book book) {
        return QuestionHelper.getDaysUntilFinish(book);
    }

    /**
     * 남은 기간동안 하루에 몇 페이지를 읽어야 하는지 계산하는 함수
     *
     * @param book 현재 독서 중인 도서 객체
     * @param review 현재 작성중인 독후감 객체
     * @param remainDate 남은 날짜
     * @return 하루에 읽어야 하는 평균 페이지 반환
     */
    public static int calculateAveragePages(Book book, Review review, int remainDate) {
        return (book.getTotalPage() - review.getPages()) / remainDate;
    }

    /**
     * 유저에게 최종 반환되는 AI 반환 객체
     *
     * @param book 도서 객체
     * @param review 독후감 객체
     * @param member 멤버 객체
     * @param remainDate 남은 날짜
     * @param averagePages 하루 평균 읽어야 하는 페이지 수
     * @param aiMessage AI 반환 값
     * @return AI 반환 객체
     */
    public static AiResponse buildAiResponse(Book book, Review review, Member member, int remainDate, int averagePages, String aiMessage) {
        return AiResponse.builder()
                .totalPages(book.getTotalPage())
                .currentPages(review.getPages())
                .progress(review.getProgress())
                .finishDate(book.getFinishDate())
                .remainDate(remainDate)
                .averageMessage(member.getNickName() + "님은 남은 기간 동안 하루에 " + averagePages + "page씩 읽으면 목표를 달성할 수 있습니다.")
                .aiMessage(aiMessage)
                .build();
    }
}
