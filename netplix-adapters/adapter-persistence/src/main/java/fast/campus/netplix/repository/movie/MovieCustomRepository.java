package fast.campus.netplix.repository.movie;

import fast.campus.netplix.entity.movie.MovieEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MovieCustomRepository {
    Optional<MovieEntity> findByMovieName(String name);

    Page<MovieEntity> searchByKeyword(String keyword, Pageable pageable);

    Page<MovieEntity> search(Pageable pageable);

    Page<MovieEntity> searchByContentType(String contentType, Pageable pageable);

    Page<MovieEntity> searchByContentTypeAndGenre(String contentType, String genre, Pageable pageable);

    List<MovieEntity> findByMovieNameIn(List<String> movieNames);

    List<MovieEntity> findByGenresExcludingMovieNames(String contentType, List<String> genres, List<String> excludeMovieNames, int limit);

    long countByContentTypeAndGenre(String contentType, String genre);

    Page<MovieEntity> searchAdvanced(String contentType, String genre, String filter, Pageable pageable);

    long countAdvanced(String contentType, String genre, String filter);
}
