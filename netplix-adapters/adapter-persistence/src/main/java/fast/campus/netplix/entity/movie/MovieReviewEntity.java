package fast.campus.netplix.entity.movie;

import fast.campus.netplix.entity.audit.MutableBaseEntity;
import fast.campus.netplix.movie.MovieReview;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "movie_reviews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MovieReviewEntity extends MutableBaseEntity {

    @Id
    @Column(name = "REVIEW_ID")
    private String reviewId;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "MOVIE_NAME")
    private String movieName;

    @Column(name = "CONTENT_TYPE")
    private String contentType;

    @Column(name = "ONE_LINER")
    private String oneLiner;

    @Column(name = "FULL_REVIEW", columnDefinition = "TEXT")
    private String fullReview;

    @Column(name = "IS_SPOILER")
    private Boolean isSpoiler;

    @Column(name = "HELPFUL_COUNT")
    private Integer helpfulCount;

    public MovieReview toDomain() {
        return MovieReview.builder()
                .reviewId(reviewId)
                .userId(userId)
                .movieName(movieName)
                .contentType(contentType)
                .oneLiner(oneLiner)
                .fullReview(fullReview)
                .isSpoiler(isSpoiler)
                .helpfulCount(helpfulCount != null ? helpfulCount : 0)
                .build();
    }

    public static MovieReviewEntity fromDomain(MovieReview domain) {
        return new MovieReviewEntity(
                domain.getReviewId(),
                domain.getUserId(),
                domain.getMovieName(),
                domain.getContentType(),
                domain.getOneLiner(),
                domain.getFullReview(),
                domain.getIsSpoiler(),
                domain.getHelpfulCount()
        );
    }
}
