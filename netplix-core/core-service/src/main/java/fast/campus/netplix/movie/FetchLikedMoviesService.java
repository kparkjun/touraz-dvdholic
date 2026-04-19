package fast.campus.netplix.movie;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FetchLikedMoviesService implements FetchLikedMoviesUseCase {

    private final LikeMoviePort likeMoviePort;
    private final PersistenceMoviePort persistenceMoviePort;

    @Override
    public List<NetplixMovie> fetchLikedMovies(String userId) {
        List<String> likedMovieIds = likeMoviePort.findLikedMovieIdsByUserId(userId);
        if (likedMovieIds == null || likedMovieIds.isEmpty()) return List.of();
        return persistenceMoviePort.fetchByMovieNames(likedMovieIds);
    }
}
