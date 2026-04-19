package fast.campus.netplix.movie;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class MovieReview {
    private final String reviewId;
    private final String userId;
    private final String movieName;
    private final String contentType;
    private String oneLiner;
    private String fullReview;
    private Boolean isSpoiler;
    @Builder.Default
    private int helpfulCount = 0;

    public static MovieReview create(String userId, String movieName, String contentType,
                                     String oneLiner, String fullReview, Boolean isSpoiler) {
        return MovieReview.builder()
                .reviewId(UUID.randomUUID().toString())
                .userId(userId)
                .movieName(movieName)
                .contentType(contentType)
                .oneLiner(oneLiner)
                .fullReview(fullReview)
                .isSpoiler(isSpoiler != null ? isSpoiler : false)
                .helpfulCount(0)
                .build();
    }
}
