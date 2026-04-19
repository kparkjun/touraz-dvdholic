package fast.campus.netplix.rag;

import fast.campus.netplix.movie.GetMoviesForRagUseCase;
import fast.campus.netplix.movie.MovieRagPort;
import fast.campus.netplix.movie.NetplixMovie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 앱 기동 후 RAG 벡터 저장소에 영화 문서를 적재한다.
 * OpenAI API 키가 설정된 경우에만 동작하며, 최대 500편을 적재한다.
 */
@Slf4j
@Component
@Order(100)
@RequiredArgsConstructor
public class MovieRagLoader implements ApplicationRunner {

    private static final int MAX_MOVIES_FOR_RAG = 500;

    private final GetMoviesForRagUseCase getMoviesForRagUseCase;
    private final MovieRagPort movieRagPort;

    @Override
    public void run(ApplicationArguments args) {
        if (!movieRagPort.isAvailable()) {
            log.info("RAG 비활성 상태라 영화 문서 적재를 건너뜁니다. (OpenAI API 키 설정 시 활성화)");
            return;
        }
        try {
            List<NetplixMovie> movies = getMoviesForRagUseCase.getMovies(MAX_MOVIES_FOR_RAG);
            if (movies.isEmpty()) {
                log.info("RAG 적재할 영화가 없습니다.");
                return;
            }
            movieRagPort.addMovieDocuments(movies);
        } catch (Exception e) {
            log.warn("RAG 영화 문서 적재 중 오류 (무시하고 계속): {}", e.getMessage());
        }
    }
}
