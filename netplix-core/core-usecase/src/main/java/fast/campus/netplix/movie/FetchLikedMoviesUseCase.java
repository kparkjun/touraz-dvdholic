package fast.campus.netplix.movie;

import fast.campus.netplix.movie.response.MoviePageableResponse;

import java.util.List;

public interface FetchLikedMoviesUseCase {
    List<NetplixMovie> fetchLikedMovies(String userId);
}
