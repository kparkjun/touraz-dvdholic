package fast.campus.netplix.rag;

import fast.campus.netplix.movie.MovieRagPort;
import fast.campus.netplix.movie.NetplixMovie;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * RAG 기본 구현(비활성). Spring AI 연동 시 다른 MovieRagPort 빈으로 교체 가능.
 * 공모전 구색: GET /api/v1/movie/recommend/rag 는 동작하며, API 키 없으면 빈 목록을 반환한다.
 */
@Component
public class NoOpMovieRagPort implements MovieRagPort {

    @Override
    public void addMovieDocuments(List<NetplixMovie> movies) {
        // no-op
    }

    @Override
    public List<String> findSimilarMovieNames(String query, int topK) {
        return Collections.emptyList();
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
