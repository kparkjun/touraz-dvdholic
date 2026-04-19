package fast.campus.netplix.movie;

import java.util.List;

/**
 * TMDB 트렌딩/인기 영화 데이터를 가져오는 포트.
 * 주인장추천 RAG 컨텍스트 구성에 사용된다.
 */
public interface TmdbTrendingPort {

    /**
     * TMDB 주간 트렌딩 영화 목록 (제목, 장르, 평점, 개봉일 등 요약 텍스트).
     * 외부 API 호출 실패 시 빈 목록 반환.
     */
    List<String> fetchWeeklyTrending(int maxPages);

    /**
     * TMDB 인기 영화 목록 요약 텍스트.
     */
    List<String> fetchPopular(int maxPages);
}
