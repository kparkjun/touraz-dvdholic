package fast.campus.netplix.movie;

import java.util.List;

public interface LikeMovieUseCase {
    Boolean like(String userId, String movieId, String contentType);
    Boolean unlike(String userId, String movieId, String contentType);
    Boolean meh(String userId, String movieId, String contentType);
    Long getLikeCount(String movieId, String contentType);
    Long getUnlikeCount(String movieId, String contentType);
    Long getMehCount(String movieId, String contentType);
    /** @return "like", "unlike", "meh", or null if no vote */
    String getMyVote(String userId, String movieId, String contentType);

    List<NetplixMovie> getTodayPopular(String contentType, int limit);
    List<NetplixMovie> getPopular(String period, String contentType, int limit);
}
