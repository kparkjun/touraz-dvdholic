package fast.campus.netplix.batch;

import fast.campus.netplix.movie.NetplixMovie;
import fast.campus.netplix.movie.NetplixPageableMovies;
import fast.campus.netplix.movie.TmdbMoviePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;

import java.util.LinkedList;
import java.util.Queue;

@Slf4j
@RequiredArgsConstructor
public class HttpPageItemReaderForDvd implements ItemReader<NetplixMovie> {
    private final int maxPageCnt;
    private final TmdbMoviePort tmdbMoviePort;

    private Queue<NetplixMovie> netplixMovieQueue = new LinkedList<>();
    private int currentPage = 1;  // TMDB API는 1부터 시작
    private int totalProcessed = 0;
    private static final int MAX_PAGE = 500; // TMDB API 최대 페이지

    @Override
    public NetplixMovie read() {
        while (netplixMovieQueue.isEmpty()) {
            if (currentPage > maxPageCnt || currentPage > MAX_PAGE) {
                log.info("========== All pages processed. Total DVDs: {} ==========", totalProcessed);
                return null;
            }

            log.info("========== Fetching page {} (DVD from TMDB API) ==========", currentPage);
            try {
                NetplixPageableMovies pageableMovies = tmdbMoviePort.fetchPageable(currentPage);
                if (pageableMovies != null && pageableMovies.getNetplixMovies() != null && !pageableMovies.getNetplixMovies().isEmpty()) {
                    log.info("Fetched {} enriched DVDs from TMDB page {}", pageableMovies.getNetplixMovies().size(), currentPage);
                    netplixMovieQueue.addAll(pageableMovies.getNetplixMovies());
                    totalProcessed += pageableMovies.getNetplixMovies().size();
                } else {
                    log.info("No more DVDs to fetch");
                    return null;
                }
                currentPage++;
                break;
            } catch (Exception e) {
                log.error("Error fetching DVDs from page {}: {}", currentPage, e.getMessage(), e);
                return null;
            }
        }

        return netplixMovieQueue.poll();
    }
}
