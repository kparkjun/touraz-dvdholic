package fast.campus.netplix.movie;

import java.util.List;

public interface RecommendMovieUseCase {
    List<NetplixMovie> getRecommendedMovies(String userId, String contentType, int limit);
}
