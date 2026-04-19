package fast.campus.netplix.movie;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendMovieService (TMDB 기반 추천)")
class RecommendMovieServiceTest {

    private static final String USER_ID = "user-1";
    private static final String CONTENT_TYPE = "movie";
    private static final int LIMIT = 5;

    @Mock
    private LikeMoviePort likeMoviePort;

    @Mock
    private PersistenceMoviePort persistenceMoviePort;

    @InjectMocks
    private RecommendMovieService recommendMovieService;

    private static NetplixMovie movie(String name, String genre, String recommendations) {
        return NetplixMovie.builder()
                .movieName(name)
                .genre(genre)
                .recommendations(recommendations)
                .contentType(CONTENT_TYPE)
                .isAdult(false)
                .overview("")
                .releasedAt(null)
                .posterPath(null)
                .backdropPath(null)
                .voteAverage(null)
                .cast(null)
                .director(null)
                .runtime(null)
                .releaseDate(null)
                .certification(null)
                .budget(null)
                .revenue(null)
                .trailerUrl(null)
                .ottProviders(null)
                .collection(null)
                .topReview(null)
                .tagline(null)
                .originalTitle(null)
                .originalLanguage(null)
                .productionCountries(null)
                .productionCompanies(null)
                .imdbId(null)
                .voteCount(null)
                .spokenLanguages(null)
                .homepage(null)
                .build();
    }

    @Test
    @DisplayName("좋아요 없으면 인기순 목록 반환")
    void noLikedMovies_returnsPopular() {
        when(likeMoviePort.findLikedMovieIdsByUserId(USER_ID)).thenReturn(List.of());
        when(persistenceMoviePort.fetchByContentType(CONTENT_TYPE, 0, LIMIT))
                .thenReturn(List.of(
                        movie("Popular1", "Action", null),
                        movie("Popular2", "Comedy", null)
                ));

        List<NetplixMovie> result = recommendMovieService.getRecommendedMovies(USER_ID, CONTENT_TYPE, LIMIT);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMovieName()).isEqualTo("Popular1");
        verify(persistenceMoviePort).fetchByContentType(CONTENT_TYPE, 0, LIMIT);
    }

    @Test
    @DisplayName("좋아요 영화의 TMDB recommendations를 우선 추천")
    void usesTmdbRecommendationsFirst() {
        when(likeMoviePort.findLikedMovieIdsByUserId(USER_ID)).thenReturn(List.of("Inception"));
        NetplixMovie liked = movie("Inception", "Sci-Fi, Action", "Interstellar, The Matrix, Shutter Island");
        when(persistenceMoviePort.fetchByMovieNames(List.of("Inception"))).thenReturn(List.of(liked));

        NetplixMovie rec1 = movie("Interstellar", "Sci-Fi", null);
        NetplixMovie rec2 = movie("The Matrix", "Sci-Fi", null);
        when(persistenceMoviePort.fetchByMovieNames(List.of("Interstellar", "The Matrix", "Shutter Island")))
                .thenReturn(List.of(rec1, rec2));
        when(persistenceMoviePort.fetchByGenresExcludingMovieNames(eq(CONTENT_TYPE), anyList(), anyList(), anyInt()))
                .thenReturn(List.of());
        when(persistenceMoviePort.fetchByContentType(eq(CONTENT_TYPE), eq(0), anyInt())).thenReturn(List.of());

        List<NetplixMovie> result = recommendMovieService.getRecommendedMovies(USER_ID, CONTENT_TYPE, LIMIT);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMovieName()).isEqualTo("Interstellar");
        assertThat(result.get(1).getMovieName()).isEqualTo("The Matrix");
        verify(persistenceMoviePort).fetchByMovieNames(List.of("Inception"));
        verify(persistenceMoviePort).fetchByMovieNames(List.of("Interstellar", "The Matrix", "Shutter Island"));
    }

    @Test
    @DisplayName("TMDB 추천 부족 시 장르 기반으로 보충")
    void fillsWithGenreBasedWhenTmdbRecsNotEnough() {
        when(likeMoviePort.findLikedMovieIdsByUserId(USER_ID)).thenReturn(List.of("Inception"));
        NetplixMovie liked = movie("Inception", "Sci-Fi, Action", "Interstellar");
        when(persistenceMoviePort.fetchByMovieNames(List.of("Inception"))).thenReturn(List.of(liked));
        when(persistenceMoviePort.fetchByMovieNames(List.of("Interstellar")))
                .thenReturn(List.of(movie("Interstellar", "Sci-Fi", null)));
        when(persistenceMoviePort.fetchByGenresExcludingMovieNames(
                eq(CONTENT_TYPE), anyList(), anyList(), eq(4)))
                .thenReturn(List.of(
                        movie("Blade Runner", "Sci-Fi", null),
                        movie("Dune", "Sci-Fi", null)
                ));
        when(persistenceMoviePort.fetchByContentType(eq(CONTENT_TYPE), eq(0), anyInt())).thenReturn(List.of());

        List<NetplixMovie> result = recommendMovieService.getRecommendedMovies(USER_ID, CONTENT_TYPE, 5);

        assertThat(result).hasSize(3); // Interstellar + 2 genre-based
        assertThat(result.get(0).getMovieName()).isEqualTo("Interstellar");
        assertThat(result.get(1).getMovieName()).isEqualTo("Blade Runner");
        assertThat(result.get(2).getMovieName()).isEqualTo("Dune");
        verify(persistenceMoviePort).fetchByGenresExcludingMovieNames(eq(CONTENT_TYPE), anyList(), anyList(), eq(4));
    }

    @Test
    @DisplayName("recommendations가 null/빈 문자열이면 장르 기반 추천만 사용")
    void noTmdbRecs_fallsBackToGenre() {
        when(likeMoviePort.findLikedMovieIdsByUserId(USER_ID)).thenReturn(List.of("SomeMovie"));
        NetplixMovie liked = movie("SomeMovie", "Drama, Romance", null);
        when(persistenceMoviePort.fetchByMovieNames(List.of("SomeMovie"))).thenReturn(List.of(liked));
        when(persistenceMoviePort.fetchByGenresExcludingMovieNames(
                eq(CONTENT_TYPE), anyList(), anyList(), eq(LIMIT)))
                .thenReturn(List.of(
                        movie("Titanic", "Drama", null),
                        movie("Notebook", "Romance", null)
                ));
        when(persistenceMoviePort.fetchByContentType(eq(CONTENT_TYPE), eq(0), anyInt())).thenReturn(List.of());

        List<NetplixMovie> result = recommendMovieService.getRecommendedMovies(USER_ID, CONTENT_TYPE, LIMIT);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMovieName()).isEqualTo("Titanic");
        assertThat(result.get(1).getMovieName()).isEqualTo("Notebook");
        verify(persistenceMoviePort).fetchByGenresExcludingMovieNames(
                eq(CONTENT_TYPE), anyList(), anyList(), eq(LIMIT));
    }
}
