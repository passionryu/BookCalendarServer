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
     *독후감 작성 인터페이스
     *
     * @param customUserDetails
     * @param reviewRequest
     * @return
     */
    QuestionResponse writeReview(CustomUserDetails customUserDetails, ReviewRequest reviewRequest);

    /**
     * 캘린더에서 날짜 선택 후 독후감 조회 인터페이스
     *
     * @param customUserDetails
     * @param date
     * @return
     */
    ReviewByDateResponse getReviewByDate(CustomUserDetails customUserDetails, LocalDate date);

    /**
     * 메인 페이지 독후감 진행률 & 남은 독서일 조회 인터페이스
     *
     * @param customUserDetails
     * @return
     */
    MainPageResponse mainPage(CustomUserDetails customUserDetails);

    /**
     * 캘린더에 독후감 진행률 조회 인터페이스
     *
     * @param customUserDetails
     * @return
     */
    List<CalendarResponse> calendar(CustomUserDetails customUserDetails, Integer month);

}
