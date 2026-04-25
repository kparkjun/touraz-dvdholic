package fast.campus.netplix.tour;

import java.util.List;

/**
 * 한국관광공사 "관광지별 연관 관광지 정보"(TarRlteTarService1) 조회 유스케이스.
 *
 * <p>"조용한 명소" 같은 사용자 추천 결과 옆에 "함께 가면 좋은 유명 명소"를
 * 데이터 기반으로 곁들여 보여주는 화면(/related-spots) 에서 사용한다.
 */
public interface GetRelatedTouristSpotUseCase {

    /** 관광지/지역 키워드로 연관 관광지 목록 조회. */
    List<RelatedTouristSpot> byKeyword(String keyword, int limit);

    /** 광역(+선택적 시·군·구) 코드로 연관 관광지 목록 조회. */
    List<RelatedTouristSpot> byArea(String areaCode, String signguCode, int limit);

    boolean isConfigured();
}
