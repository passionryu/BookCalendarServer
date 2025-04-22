package bookcalendar.server.Domain.Review.Service;


import bookcalendar.server.Domain.Book.Entity.Book;
import bookcalendar.server.Domain.Book.Repository.BookRepository;
import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Exception.MemberException;
import bookcalendar.server.Domain.Member.Repository.MemberRepository;
import bookcalendar.server.Domain.Question.Entity.Question;
import bookcalendar.server.Domain.Question.Repository.QuestionRepository;
import bookcalendar.server.Domain.Review.DTO.Request.ReviewRequest;
import bookcalendar.server.Domain.Review.DTO.Response.QuestionNumberOneResponse;
import bookcalendar.server.Domain.Review.DTO.Response.QuestionNumberTwoThreeResponse;
import bookcalendar.server.Domain.Review.DTO.Response.QuestionResponse;
import bookcalendar.server.Domain.Review.Entity.Review;
import bookcalendar.server.Domain.Review.Repository.ReviewRepository;
import bookcalendar.server.global.Security.CustomUserDetails;
import bookcalendar.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final QuestionRepository questionRepository;
    private final ChatClient chatClient;

    /**
     * 독후감 작성 메서드
     *
     * @param customUserDetails
     * @param reviewRequest
     * @return 독후감에 대한 AI 질문 3가지
     */
    @Override
    public QuestionResponse writeReview(CustomUserDetails customUserDetails, ReviewRequest reviewRequest) {

        // 요청에서 본문 추출
        String contents = reviewRequest.contents();
        Integer pages = reviewRequest.pages();

        // 현재 유저의 독서중인 도서 객체 반환
        Book book = bookRepository.findByMemberIdAndStatus( customUserDetails.getMemberId(), Book.Status.독서중)
                .orElseThrow(()->new MemberException(ErrorCode.USER_NOT_FOUND) );

        // 현재 유저의 멤버 객체 반환
        Member member = memberRepository.findByMemberId(customUserDetails.getMemberId())
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));

        // Progress 계산 로직
        //Integer progress = (pages / book.getTotalPage())* 100;
        Integer progress = (int)(((double) pages / book.getTotalPage()) * 100);

        log.info("book.getTotalPage() : {}", book.getTotalPage());
        log.info("progress : {}", progress);

        /* 1번 질문지 생성 AI 모델로 요청 */
        QuestionNumberOneResponse questionNumberOneResponse = numberOneQuestion(contents);

        /* 2번 질문지 생성 AI 모델로 요청 */
        QuestionNumberTwoThreeResponse questionNumberTwoThreeResponse = numberTwoThreeQuestion(contents);

        /* 결과 Review DB에 저장 하는 로직 */
        Review savedReview = reviewRepository.save(Review.builder()
                        .contents(contents)
                        .progress(progress)
                        .pages(pages)
                        .member(member)
                        .book(book)
                        .date(LocalDate.now())
                        .build());

        // reviewId 반환
        Integer reviewId = savedReview.getReviewId();

        /* 결과를 Question DB에 저장하는 로직 */
        Question question = questionRepository.save(Question.builder()
                        .review(savedReview)
                        .member(member)
                        .question1(questionNumberOneResponse.question1())
                        .question2(questionNumberTwoThreeResponse.question2())
                        .question3(questionNumberTwoThreeResponse.question3())
                        .feedback1(0)
                        .feedback2(0)
                        .feedback3(0)
                        .build());

        // questionId 반환
        Integer questionId = question.getQuestionId();

        return new QuestionResponse(reviewId,questionId,
                questionNumberOneResponse.question1(),
                questionNumberTwoThreeResponse.question2(),
                questionNumberTwoThreeResponse.question3());
    }

    // ======================= AI 모델 임시 대체 private 메서드 =========================

    /**
     * 감정 추출 후 1번 질문지 생성 모델 임시 대체 메서드
     *
     * @param contents
     * @return
     */
    private QuestionNumberOneResponse numberOneQuestion(String contents){

        String contensPrompt = "다음 글은 사용자의 독후감이다."+
                "독후감을 보고 사용자의 감정을 추출하되, (기쁨, 당황,분노, 불안, 슬픔) 중 하나로 분류해줘 ->" + contents;

        String emotion = chatClient.call(contensPrompt);
        log.info("emotion : {}", emotion);

        String questionPrompt = "다음은 사용자가 작성한 독후감에서 추출한 감정이다."+
                "이 감정값을 토대로 사용자의 감정에 관한 질문지한줄로 작성해줘." +
                " 추출된 감정 -> "+emotion;
        String question1 = chatClient.call(questionPrompt);

        return new QuestionNumberOneResponse(emotion,question1);
    }

    /**
     * 2,3번 질문지 생성에 대한 모델 임시 대체 메서드
     *
     * @param contents
     * @return
     */
    private QuestionNumberTwoThreeResponse numberTwoThreeQuestion(String contents){

        String questionPrompt = "다음은 사용자가 작성한 독후감이다."+
                "해당 독후감에 대한 사용자의 질문지를 1~2줄 길이로 하나 작성해줘 ->" + contents;

        String question2 = chatClient.call(questionPrompt);
        String question3 = chatClient.call(questionPrompt);

        return new QuestionNumberTwoThreeResponse(question2,question3);
    }


}
