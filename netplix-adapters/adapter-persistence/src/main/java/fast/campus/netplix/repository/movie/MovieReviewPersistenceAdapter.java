package fast.campus.netplix.repository.movie;

import fast.campus.netplix.entity.movie.MovieReviewEntity;
import fast.campus.netplix.movie.MovieReview;
import fast.campus.netplix.movie.MovieReviewPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MovieReviewPersistenceAdapter implements MovieReviewPort {

    private final MovieReviewJpaRepository jpaRepository;

    @Override
    @Transactional
    public MovieReview save(MovieReview review) {
        MovieReviewEntity entity = MovieReviewEntity.fromDomain(review);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieReview> findByMovieNameAndContentType(String movieName, String contentType) {
        return jpaRepository.findByMovieNameAndContentTypeOrderByReviewIdDesc(movieName, contentType)
                .stream().map(MovieReviewEntity::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieReview> findByMovieNameAndContentTypeSortByHelpful(String movieName, String contentType) {
        return jpaRepository.findByMovieNameAndContentTypeOrderByHelpful(movieName, contentType)
                .stream().map(MovieReviewEntity::toDomain).toList();
    }

    @Override
    @Transactional
    public void deleteByReviewIdAndUserId(String reviewId, String userId) {
        jpaRepository.deleteByReviewIdAndUserId(reviewId, userId);
    }

    @Override
    @Transactional
    public void incrementHelpfulCount(String reviewId) {
        jpaRepository.incrementHelpfulCount(reviewId);
    }
}
