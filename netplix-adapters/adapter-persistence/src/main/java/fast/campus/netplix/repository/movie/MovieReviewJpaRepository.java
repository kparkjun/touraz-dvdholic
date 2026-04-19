package fast.campus.netplix.repository.movie;

import fast.campus.netplix.entity.movie.MovieReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovieReviewJpaRepository extends JpaRepository<MovieReviewEntity, String> {

    List<MovieReviewEntity> findByMovieNameAndContentTypeOrderByReviewIdDesc(String movieName, String contentType);

    @Query("SELECT r FROM MovieReviewEntity r WHERE r.movieName = :movieName AND r.contentType = :contentType ORDER BY r.helpfulCount DESC, r.reviewId DESC")
    List<MovieReviewEntity> findByMovieNameAndContentTypeOrderByHelpful(@Param("movieName") String movieName, @Param("contentType") String contentType);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE MovieReviewEntity r SET r.helpfulCount = r.helpfulCount + 1 WHERE r.reviewId = :reviewId")
    void incrementHelpfulCount(@Param("reviewId") String reviewId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM MovieReviewEntity r WHERE r.reviewId = :reviewId AND r.userId = :userId")
    void deleteByReviewIdAndUserId(@Param("reviewId") String reviewId, @Param("userId") String userId);
}
