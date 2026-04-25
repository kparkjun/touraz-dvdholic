package fast.campus.netplix.tour;

import java.util.List;

/**
 * 한국관광공사 "관광지별 연관 관광지 정보"(TarRlteTarService1) 조회 Port.
 *
 * <p>두 가지 호출 모드:
 * <ul>
 *   <li>{@link #fetchByKeyword(String, int)} — 사용자가 입력한 관광지/지역 키워드로
 *       /searchKeyword1 호출. "조용한 명소 → 함께 가는 핫플" 추천에 적합.</li>
 *   <li>{@link #fetchByArea(String, String, int)} — 시·군·구 코드로 /areaBasedList1 호출.
 *       지역 카드에서 대표 관광지 + 연관 추천을 묶어 보여줄 때 사용.</li>
 * </ul>
 *
 * <p>일일 트래픽 한도(1,000회) 보호를 위해 어댑터 측에서 in-memory TTL 캐시를 유지한다.
 */
public interface RelatedTouristSpotPort {

    List<RelatedTouristSpot> fetchByKeyword(String keyword, int limit);

    List<RelatedTouristSpot> fetchByArea(String areaCode, String signguCode, int limit);

    boolean isConfigured();
}
