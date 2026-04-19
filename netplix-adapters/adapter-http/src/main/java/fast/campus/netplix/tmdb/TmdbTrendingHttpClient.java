package fast.campus.netplix.tmdb;

import fast.campus.netplix.client.TmdbHttpClient;
import fast.campus.netplix.movie.TmdbTrendingPort;
import fast.campus.netplix.util.ObjectMapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbTrendingHttpClient implements TmdbTrendingPort {

    @Value("${tmdb.api.movie-lists.trending-week:}")
    private String trendingWeekUrl;

    @Value("${tmdb.api.movie-lists.popular:}")
    private String popularUrl;

    private final TmdbHttpClient tmdbHttpClient;

    @Override
    public List<String> fetchWeeklyTrending(int maxPages) {
        return fetchMovieSummaries(trendingWeekUrl, maxPages, "trending");
    }

    @Override
    public List<String> fetchPopular(int maxPages) {
        return fetchMovieSummaries(popularUrl, maxPages, "popular");
    }

    private List<String> fetchMovieSummaries(String baseUrl, int maxPages, String label) {
        if (baseUrl == null || baseUrl.isBlank()) {
            log.debug("TMDB {} URL 미설정", label);
            return List.of();
        }
        List<String> summaries = new ArrayList<>();
        for (int page = 1; page <= maxPages; page++) {
            try {
                String url = baseUrl + "&language=ko-KR&page=" + page;
                String raw = tmdbHttpClient.request(url, HttpMethod.GET,
                        CollectionUtils.toMultiValueMap(Map.of()), Map.of());
                TmdbResponse resp = ObjectMapperUtil.toObject(raw, TmdbResponse.class);
                if (resp == null || resp.getResults() == null || resp.getResults().isEmpty()) break;

                for (TmdbMovieNowPlaying m : resp.getResults()) {
                    String genre = (m.getGenreIds() != null && !m.getGenreIds().isEmpty())
                            ? TmdbMovieGenre.getGenreNamesByIds(m.getGenreIds())
                            : "Unknown";
                    String vote = m.getVoteAverage() != null
                            ? String.format("%.1f", m.getVoteAverage()) : "?";
                    String summary = String.format("%s (장르: %s, 평점: %s, 개봉: %s)",
                            m.getTitle(), genre, vote,
                            m.getReleaseDate() != null ? m.getReleaseDate() : "미정");
                    summaries.add(summary);
                }
                log.info("TMDB {} 페이지 {} 가져옴 (누적 {}편)", label, page, summaries.size());
            } catch (Exception e) {
                log.warn("TMDB {} 페이지 {} 실패: {}", label, page, e.getMessage());
                break;
            }
        }
        return summaries;
    }
}
