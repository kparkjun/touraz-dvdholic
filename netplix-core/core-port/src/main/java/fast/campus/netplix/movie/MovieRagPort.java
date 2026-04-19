package fast.campus.netplix.movie;

import java.util.List;

/**
 * RAG(벡터 검색) 기반 영화 추천을 위한 포트.
 * 영화 목록을 문서로 적재하고, 질의와 유사한 영화 제목을 검색한다.
 */
public interface MovieRagPort {

    /**
     * 영화 목록을 벡터 저장소에 문서로 적재한다.
     */
    void addMovieDocuments(List<NetplixMovie> movies);

    /**
     * 질의와 유사한 영화 제목 목록을 유사도 순으로 반환한다.
     *
     * @param query 사용자 질의 (예: "심리 스릴러 추천")
     * @param topK  반환할 최대 개수
     * @return 영화 제목 목록
     */
    List<String> findSimilarMovieNames(String query, int topK);

    /**
     * RAG 인프라(벡터 저장소·임베딩)가 사용 가능한지 여부.
     */
    boolean isAvailable();
}
