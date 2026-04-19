package fast.campus.netplix.movie;

import java.util.List;

/**
 * RAG 문서 적재용 영화 목록을 조회하는 유스케이스.
 */
public interface GetMoviesForRagUseCase {

    /**
     * 벡터 저장소 적재용 영화 목록을 페이지네이션으로 가져온다.
     *
     * @param maxCount 최대 영화 수 (dvd + movie 합산)
     * @return 영화 목록
     */
    List<NetplixMovie> getMovies(int maxCount);
}
