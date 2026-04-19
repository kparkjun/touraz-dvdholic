package fast.campus.netplix.movie;

import java.util.List;

public interface MovieReviewUseCase {
    MovieReview saveReview(String userId, String movieName, String contentType,
                           String oneLiner, String fullReview, Boolean isSpoiler);
    List<MovieReview> getReviews(String movieName, String contentType, String sort);
    void deleteReview(String reviewId, String userId);
    void markHelpful(String reviewId);
}
