package fast.campus.netplix.movie;

import java.util.List;

public interface MovieReviewPort {
    MovieReview save(MovieReview review);
    List<MovieReview> findByMovieNameAndContentType(String movieName, String contentType);
    List<MovieReview> findByMovieNameAndContentTypeSortByHelpful(String movieName, String contentType);
    void deleteByReviewIdAndUserId(String reviewId, String userId);
    void incrementHelpfulCount(String reviewId);
}
