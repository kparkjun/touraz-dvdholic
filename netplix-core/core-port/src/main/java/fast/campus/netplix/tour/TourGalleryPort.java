package fast.campus.netplix.tour;

import java.util.List;

/**
 * 한국관광공사 관광사진갤러리(PhotoGalleryService1) 조회 Port.
 *
 * <p>개별 영화/지역/DVD 매장 "현장감" 보강용 사진 리소스.
 * - 키워드 검색이 핵심 접점 (영화 지역 매핑의 지역명, 매장 소재지 시·도 등)
 * - 일일 쿼터 1,000회 고려하여 24시간 in-memory 캐시로 보관
 */
public interface TourGalleryPort {

    /**
     * 키워드 검색. keyword 공백이면 fetchAll 과 동일.
     *
     * @param keyword 한국어 키워드 (예: "부산", "해운대", "남산")
     * @param limit   최대 반환 개수
     */
    List<TourGallery> fetchByKeyword(String keyword, int limit);

    /**
     * 전체 상위 N 개 (최신 등록 순).
     */
    List<TourGallery> fetchAll(int limit);

    /**
     * 어댑터가 호출 가능한 상태인지 (serviceKey 설정 여부).
     */
    boolean isConfigured();
}
