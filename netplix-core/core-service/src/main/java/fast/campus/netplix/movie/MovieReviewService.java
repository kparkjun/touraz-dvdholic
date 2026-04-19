package fast.campus.netplix.movie;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieReviewService implements MovieReviewUseCase {

    private final MovieReviewPort movieReviewPort;

    @Override
    public MovieReview saveReview(String userId, String movieName, String contentType,
                                  String oneLiner, String fullReview, Boolean isSpoiler) {
        log.info("리뷰 저장 - userId: {}, movie: {}, contentType: {}", userId, movieName, contentType);
        MovieReview review = MovieReview.create(userId, movieName, contentType, oneLiner, fullReview, isSpoiler);
        return movieReviewPort.save(review);
    }

    @Override
    public List<MovieReview> getReviews(String movieName, String contentType, String sort) {
        if ("helpful".equals(sort)) {
            return movieReviewPort.findByMovieNameAndContentTypeSortByHelpful(movieName, contentType);
        }
        return movieReviewPort.findByMovieNameAndContentType(movieName, contentType);
    }

    @Override
    public void deleteReview(String reviewId, String userId) {
        movieReviewPort.deleteByReviewIdAndUserId(reviewId, userId);
    }

    @Override
    public void markHelpful(String reviewId) {
        movieReviewPort.incrementHelpfulCount(reviewId);
    }
}
