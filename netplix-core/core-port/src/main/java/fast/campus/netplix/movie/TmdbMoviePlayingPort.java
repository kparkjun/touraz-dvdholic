package fast.campus.netplix.movie;

public interface TmdbMoviePlayingPort {
    NetplixPageableMovies fetchPageable(int page);
    NetplixMovie enrichMovieDetails(NetplixMovie movie, Integer tmdbId);
}
