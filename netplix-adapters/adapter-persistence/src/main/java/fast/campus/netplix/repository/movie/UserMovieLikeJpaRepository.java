package fast.campus.netplix.repository.movie;

import fast.campus.netplix.entity.movie.UserMovieLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserMovieLikeJpaRepository extends JpaRepository<UserMovieLikeEntity, String> {
    Optional<UserMovieLikeEntity> findByUserIdAndMovieIdAndContentType(String userId, String movieId, String contentType);

    @Query("SELECT u.movieId FROM UserMovieLikeEntity u WHERE u.userId = :userId AND (u.voteType = 'like' OR (u.voteType IS NULL AND u.likeYn = true))")
    List<String> findLikedMovieIdsByUserId(@Param("userId") String userId);

    @Query("SELECT COUNT(u) FROM UserMovieLikeEntity u WHERE u.movieId = :movieId AND u.contentType = :contentType AND (u.voteType = 'like' OR (u.voteType IS NULL AND u.likeYn = true))")
    Long countLikesByMovieIdAndContentType(@Param("movieId") String movieId, @Param("contentType") String contentType);

    @Query("SELECT COUNT(u) FROM UserMovieLikeEntity u WHERE u.movieId = :movieId AND u.contentType = :contentType AND (u.voteType = 'unlike' OR (u.voteType IS NULL AND u.likeYn = false))")
    Long countUnlikesByMovieIdAndContentType(@Param("movieId") String movieId, @Param("contentType") String contentType);

    @Query("SELECT COUNT(u) FROM UserMovieLikeEntity u WHERE u.movieId = :movieId AND u.contentType = :contentType AND u.voteType = 'meh'")
    Long countMehsByMovieIdAndContentType(@Param("movieId") String movieId, @Param("contentType") String contentType);

    @Modifying
    @Query("DELETE FROM UserMovieLikeEntity u WHERE u.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM UserMovieLikeEntity u WHERE u.userId = :userId AND u.movieId = :movieId AND u.contentType = :contentType")
    void deleteByUserIdAndMovieIdAndContentType(@Param("userId") String userId, @Param("movieId") String movieId, @Param("contentType") String contentType);

    @Query("SELECT u.movieId FROM UserMovieLikeEntity u " +
           "WHERE u.contentType = :contentType " +
           "AND (u.voteType = 'like' OR (u.voteType IS NULL AND u.likeYn = true)) " +
           "AND COALESCE(u.createdAt, u.modifiedAt) >= :since " +
           "GROUP BY u.movieId ORDER BY COUNT(u) DESC")
    List<String> findTopLikedMovieIdsSince(
            @Param("contentType") String contentType,
            @Param("since") LocalDateTime since,
            org.springframework.data.domain.Pageable pageable);

    @Query("SELECT u.movieId FROM UserMovieLikeEntity u " +
           "WHERE u.contentType IN ('movie', 'dvd') " +
           "AND (u.voteType = 'like' OR (u.voteType IS NULL AND u.likeYn = true)) " +
           "AND COALESCE(u.createdAt, u.modifiedAt) >= :since " +
           "GROUP BY u.movieId ORDER BY COUNT(u) DESC")
    List<String> findTopLikedMovieIdsSinceCombiningMovieAndDvd(
            @Param("since") LocalDateTime since,
            org.springframework.data.domain.Pageable pageable);
}
