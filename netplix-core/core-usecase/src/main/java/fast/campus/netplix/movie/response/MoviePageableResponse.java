package fast.campus.netplix.movie.response;

import fast.campus.netplix.movie.NetplixMovie;
import lombok.Getter;

import java.util.List;

@Getter
public class MoviePageableResponse {
    private final List<NetplixMovie> movies;

    private final int page;
    private final Boolean hasNext;
    private final Long totalCount;

    public MoviePageableResponse(List<NetplixMovie> movies, int page, Boolean hasNext) {
        this.movies = movies;
        this.page = page;
        this.hasNext = hasNext;
        this.totalCount = null;
    }

    public MoviePageableResponse(List<NetplixMovie> movies, int page, Boolean hasNext, Long totalCount) {
        this.movies = movies;
        this.page = page;
        this.hasNext = hasNext;
        this.totalCount = totalCount;
    }
}
