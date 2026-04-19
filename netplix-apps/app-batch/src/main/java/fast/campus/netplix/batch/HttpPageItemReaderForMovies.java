package fast.campus.netplix.batch;

import fast.campus.netplix.movie.NetplixMovie;
import fast.campus.netplix.movie.NetplixPageableMovies;
import fast.campus.netplix.movie.TmdbMoviePlayingPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;

import java.util.LinkedList;
import java.util.Queue;

@Slf4j
@RequiredArgsConstructor
public class HttpPageItemReaderForMovies implements ItemReader<NetplixMovie> {
    private final int maxPageCnt;
    private final TmdbMoviePlayingPort tmdbMoviePlayingPort;

    private Queue<NetplixMovie> netplixMovieQueue = new LinkedList<>();
    private int currentPage = 1;  // TMDB API는 1부터 시작
    private int totalProcessed = 0;
    private static final int MAX_PAGE = 500; // TMDB API 최대 페이지
    /** 해당 페이지에서 멈춤/지연 발생 → 스킵 */
    private static final int SKIP_PAGE = 33;

    @Override
    public NetplixMovie read() {
        while (netplixMovieQueue.isEmpty()) {
            if (currentPage > maxPageCnt || currentPage > MAX_PAGE) {
                log.info("========== All pages processed. Total movies: {} ==========", totalProcessed);
                return null;
            }
            if (currentPage == SKIP_PAGE) {
                log.info("========== Skipping page {} (known hang) ==========", SKIP_PAGE);
                currentPage++;
                continue;
            }

            log.info("========== Fetching page {} (MOVIE from TMDB API) ==========", currentPage);
            try {
                NetplixPageableMovies pageableMovies = tmdbMoviePlayingPort.fetchPageable(currentPage);
                if (pageableMovies != null && pageableMovies.getNetplixMovies() != null && !pageableMovies.getNetplixMovies().isEmpty()) {
                    log.info("Fetched {} enriched movies from TMDB page {}", pageableMovies.getNetplixMovies().size(), currentPage);
                    netplixMovieQueue.addAll(pageableMovies.getNetplixMovies());
                    totalProcessed += pageableMovies.getNetplixMovies().size();
                } else {
                    log.info("No more movies to fetch");
                    return null;
                }
                currentPage++;
                break;
            } catch (Exception e) {
                log.error("Error fetching movies from page {}: {}", currentPage, e.getMessage(), e);
                return null;
            }
        }

        return netplixMovieQueue.poll();
    }
}
