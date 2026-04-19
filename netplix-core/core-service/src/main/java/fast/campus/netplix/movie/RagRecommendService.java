package fast.campus.netplix.movie;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagRecommendService implements RagRecommendUseCase {

    private final MovieRagPort movieRagPort;
    private final PersistenceMoviePort persistenceMoviePort;

    @Override
    public List<NetplixMovie> recommendByQuery(String query, String contentType, int limit) {
        if (!movieRagPort.isAvailable()) {
            log.debug("RAG 비활성 상태, 빈 목록 반환");
            return List.of();
        }
        List<String> names = movieRagPort.findSimilarMovieNames(query, limit * 2);
        if (names.isEmpty()) {
            return List.of();
        }
        List<NetplixMovie> candidates = persistenceMoviePort.fetchByMovieNames(names);
        List<NetplixMovie> result = new ArrayList<>();
        for (NetplixMovie m : candidates) {
            if (contentType != null && !contentType.equals(m.getContentType())) {
                continue;
            }
            result.add(m);
            if (result.size() >= limit) {
                break;
            }
        }
        log.info("RAG 추천: query='{}', {}편 반환", query, result.size());
        return result;
    }
}
