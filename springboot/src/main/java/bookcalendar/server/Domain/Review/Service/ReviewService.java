package bookcalendar.server.Domain.Review.Service;

import bookcalendar.server.Domain.Review.DTO.Request.ReviewRequest;
import bookcalendar.server.Domain.Review.DTO.Response.QuestionResponse;
import bookcalendar.server.global.Security.CustomUserDetails;

public interface ReviewService {

    /**
     *독후감 작성 인터페이스
     *
     * @param customUserDetails
     * @param reviewRequest
     * @return
     */
    QuestionResponse writeReview(CustomUserDetails customUserDetails, ReviewRequest reviewRequest);
}
