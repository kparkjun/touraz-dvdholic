package fast.campus.netplix.movie;

import fast.campus.netplix.movie.response.MovieWithRecommendReason;

import java.util.List;

/**
 * DVD방 주인장 추천 유스케이스.
 * TMDB 트렌딩 데이터(RAG 컨텍스트) + DB 카탈로그 + OpenAI를 결합하여
 * 주인장 페르소나로 영화를 추천한다.
 */
public interface OwnerRecommendUseCase {

    /**
     * 주인장 추천: 고객의 요청에 맞는 영화를 추천한다.
     *
     * @param customerRequest 손님 요청 (예: "요즘 잘나가는 것 중에 볼만한 게 뭐가 있을까요?")
     * @param contentType     dvd | movie
     * @param limit           최대 개수
     * @return 영화 + 주인장의 추천 이유
     */
    List<MovieWithRecommendReason> recommendByOwner(String customerRequest, String contentType, int limit);

    /**
     * TMDB 트렌딩 데이터 갱신 (스케줄러에서 호출).
     */
    void refreshTrendingContext();

    /**
     * 현재 트렌딩 컨텍스트 보유 여부.
     */
    boolean hasTrendingContext();
}
