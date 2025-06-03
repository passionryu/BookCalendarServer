package bookcalendar.server.Domain.Review.Service;

import bookcalendar.server.Domain.Review.DTO.Request.ReviewRequest;
import bookcalendar.server.Domain.Review.DTO.Response.CalendarResponse;
import bookcalendar.server.Domain.Review.DTO.Response.MainPageResponse;
import bookcalendar.server.Domain.Review.DTO.Response.QuestionResponse;
import bookcalendar.server.Domain.Review.DTO.Response.ReviewByDateResponse;
import bookcalendar.server.global.Security.CustomUserDetails;

import java.time.LocalDate;
import java.util.List;

public interface ReviewService {

    /**
     * 향상된 독후감 작성 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param reviewRequest 독후감 데이터
     * @return 질문지
     */
    QuestionResponse enhancedWriteReview(CustomUserDetails customUserDetails, ReviewRequest reviewRequest);

    /**
     *독후감 작성 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param reviewRequest 독후감 데이터
     * @return 질문지
     */
    QuestionResponse writeReview(CustomUserDetails customUserDetails, ReviewRequest reviewRequest);

    /**
     * 캘린더에서 날짜 선택 후 독후감 조회 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @param date 독후감을 보고자 하는 날짜
     * @return
     */
    ReviewByDateResponse getReviewByDate(CustomUserDetails customUserDetails, LocalDate date);

    /**
     * 메인 페이지 독후감 진행률 & 남은 독서일 조회 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return
     */
    MainPageResponse mainPage(CustomUserDetails customUserDetails);

    /**
     * 캘린더에 독후감 진행률 조회 인터페이스
     *
     * @param customUserDetails 인증된 유저의 정보 객체
     * @return
     */
    List<CalendarResponse> calendar(CustomUserDetails customUserDetails, Integer month);

}
