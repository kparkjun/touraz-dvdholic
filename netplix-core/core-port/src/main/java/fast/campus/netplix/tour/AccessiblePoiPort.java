package fast.campus.netplix.tour;

import java.util.List;
import java.util.Optional;

/**
 * 한국관광공사 무장애 여행정보(KorWithService2) 조회 Port.
 *
 * <p>포토({@link TourPhotoPort}) 와 달리 데이터 총량이 커서 전량 캐싱은 부적절.
 * 어댑터는 (areaCode, contentTypeId) 조합 키로 TTL 캐시를 유지한다.
 */
public interface AccessiblePoiPort {

    /**
     * 지역 + 콘텐츠타입 기반 목록.
     *
     * @param areaCode      KorService2 areaCode (1~8, 31~39). null/blank 허용 시 전국.
     * @param contentTypeId 12/14/25/32/39 중 하나. null/blank 면 전체 타입.
     * @param limit         최대 반환 개수 (권장 5~30).
     * @return 썸네일/주소 있는 항목 우선. 미설정/오류 시 빈 리스트.
     */
    List<AccessiblePoi> fetchByArea(String areaCode, String contentTypeId, int limit);

    /**
     * 콘텐츠ID 로 무장애 상세 조회. detailWithTour2 사용.
     * 호출 비용이 커서 즉석 UI 외에는 배치에서만 호출 권장.
     */
    Optional<AccessiblePoi> fetchDetail(String contentId, String contentTypeId);

    /**
     * 좌표 반경 기반 (CineTrip 촬영지 주변 POI 탐색용).
     *
     * @param mapX   longitude (GPS)
     * @param mapY   latitude (GPS)
     * @param radius meters (권장 1000~5000)
     */
    List<AccessiblePoi> fetchByLocation(double mapX, double mapY, int radius,
                                        String contentTypeId, int limit);

    /**
     * 키워드 검색 (영화 제목/촬영지명 → 관련 POI 매칭 실험용).
     */
    List<AccessiblePoi> fetchByKeyword(String keyword, String contentTypeId, int limit);

    /**
     * 어댑터 호출 가능 상태(serviceKey + URL 설정) 여부.
     */
    boolean isConfigured();
}
