package fast.campus.netplix.movie;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * TMDB API에서 수집한 정보(장르, recommendations)를 활용한 추천 서비스.
 * 1) 좋아요한 영화의 TMDB 추천 목록을 우선 반영
 * 2) 부족분은 사용자 선호 장르 기반으로 보충
 * 3) 그래도 부족하면 인기순 목록으로 채움
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendMovieService implements RecommendMovieUseCase {

    private static final int MAX_TMDB_RECOMMENDATION_TITLES = 30;

    private final LikeMoviePort likeMoviePort;
    private final PersistenceMoviePort persistenceMoviePort;

    @Override
    public List<NetplixMovie> getRecommendedMovies(String userId, String contentType, int limit) {
        List<String> likedMovieNames = likeMoviePort.findLikedMovieIdsByUserId(userId);
        Set<String> excludeNames = new HashSet<>(likedMovieNames);

        if (likedMovieNames.isEmpty()) {
            log.info("User {} has no liked movies, returning popular movies", userId);
            return persistenceMoviePort.fetchByContentType(contentType, 0, limit);
        }

        List<NetplixMovie> likedMovies = persistenceMoviePort.fetchByMovieNames(likedMovieNames);

        // 1) TMDB API에 저장된 "추천 영화" 목록 수집 (좋아요한 영화별 recommendations 필드)
        List<String> tmdbRecommendationTitles = collectTmdbRecommendationTitles(likedMovies, excludeNames);
        List<NetplixMovie> result = new ArrayList<>();

        if (!tmdbRecommendationTitles.isEmpty()) {
            List<NetplixMovie> byNames = persistenceMoviePort.fetchByMovieNames(tmdbRecommendationTitles);
            Map<String, NetplixMovie> byNameMap = byNames.stream()
                    .collect(Collectors.toMap(NetplixMovie::getMovieName, Function.identity(), (a, b) -> a));

            for (String title : tmdbRecommendationTitles) {
                if (result.size() >= limit) break;
                NetplixMovie movie = byNameMap.get(title);
                if (movie != null && contentType.equals(movie.getContentType()) && !excludeNames.contains(title)) {
                    result.add(movie);
                    excludeNames.add(title);
                }
            }
            log.info("User {} TMDB-based recommendations: {} of {}", userId, result.size(), limit);
        }

        // 2) 부족분은 선호 장르 기반으로 보충
        if (result.size() < limit) {
            List<String> topGenres = extractTopGenres(likedMovies, 3);
            if (!topGenres.isEmpty()) {
                int remaining = limit - result.size();
                List<NetplixMovie> byGenres = persistenceMoviePort.fetchByGenresExcludingMovieNames(
                        contentType, topGenres, new ArrayList<>(excludeNames), remaining);
                for (NetplixMovie m : byGenres) {
                    if (result.size() >= limit) break;
                    if (!excludeNames.contains(m.getMovieName())) {
                        result.add(m);
                        excludeNames.add(m.getMovieName());
                    }
                }
            }
        }

        // 3) 그래도 부족하면 인기순으로 채움
        if (result.size() < limit) {
            int remaining = limit - result.size();
            List<NetplixMovie> additional = persistenceMoviePort.fetchByContentType(contentType, 0, remaining + excludeNames.size())
                    .stream()
                    .filter(m -> !excludeNames.contains(m.getMovieName()))
                    .limit(remaining)
                    .toList();
            result.addAll(additional);
        }

        return result;
    }

    /**
     * 좋아요한 영화들의 TMDB recommendations 필드에서 추천 제목만 순서 유지하며 수집.
     */
    private List<String> collectTmdbRecommendationTitles(List<NetplixMovie> likedMovies, Set<String> excludeNames) {
        List<String> titles = new ArrayList<>();
        Set<String> seen = new HashSet<>(excludeNames);

        for (NetplixMovie m : likedMovies) {
            String rec = m.getRecommendations();
            if (rec == null || rec.isBlank()) continue;
            for (String part : rec.split(",")) {
                String title = part.trim();
                if (title.isEmpty() || !seen.add(title)) continue;
                titles.add(title);
                if (titles.size() >= MAX_TMDB_RECOMMENDATION_TITLES) return titles;
            }
        }
        return titles;
    }

    private List<String> extractTopGenres(List<NetplixMovie> movies, int topN) {
        Map<String, Long> genreCount = new HashMap<>();
        
        for (NetplixMovie movie : movies) {
            String genre = movie.getGenre();
            if (genre != null && !genre.isBlank()) {
                Arrays.stream(genre.split(","))
                        .map(String::trim)
                        .filter(g -> !g.isEmpty())
                        .forEach(g -> genreCount.merge(g, 1L, Long::sum));
            }
        }
        
        return genreCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
