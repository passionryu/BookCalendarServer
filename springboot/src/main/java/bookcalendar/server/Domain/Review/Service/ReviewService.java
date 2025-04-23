package bookcalendar.server.Domain.Review.Service;

import bookcalendar.server.Domain.Review.DTO.Request.ReviewRequest;
import bookcalendar.server.Domain.Review.DTO.Response.QuestionResponse;
import bookcalendar.server.Domain.Review.DTO.Response.ReviewByDateResponse;
import bookcalendar.server.global.Security.CustomUserDetails;

import java.time.LocalDate;

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
}
