package fast.campus.netplix.movie;

import fast.campus.netplix.movie.response.MovieWithRecommendReason;

import java.util.List;

/**
 * 프롬프트(질의) + 기분·동행 기반 추천.
 * OpenAI API로 후보 선정 후 DB에서 영화 정보를 채워 반환한다.
 */
public interface PromptRecommendUseCase {

    /**
     * 질의(q), 기분(mood), 동행(companion)에 맞는 영화를 추천한다.
     *
     * @param q          자유 질의 (예: "잔인하지 않은 호러")
     * @param mood       기분 (예: 우울, 힐링, 스트레스해소)
     * @param companion  동행 (예: 혼자, 연인, 가족)
     * @param contentType dvd | movie
     * @param limit      최대 개수
     * @return 영화 + 추천 이유 (API 미설정 시 빈 목록)
     */
    List<MovieWithRecommendReason> recommendByPrompt(
            String q, String mood, String companion, String contentType, int limit);
}
