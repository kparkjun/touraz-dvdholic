package fast.campus.netplix.movie;

import java.util.List;

/**
 * RAG(검색 기반)로 사용자 질의에 맞는 영화를 추천하는 유스케이스.
 */
public interface RagRecommendUseCase {

    /**
     * 질의와 유사한 영화를 추천한다.
     *
     * @param query       사용자 질의 (예: "로맨스 코미디")
     * @param contentType dvd | movie
     * @param limit       최대 개수
     * @return 추천 영화 목록 (없거나 RAG 비활성 시 빈 목록)
     */
    List<NetplixMovie> recommendByQuery(String query, String contentType, int limit);
}
