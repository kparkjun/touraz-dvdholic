package fast.campus.netplix.tour;

import java.util.List;
import java.util.Optional;

/**
 * 한국관광공사 무장애 여행정보 조회 유스케이스.
 * REST 컨트롤러에서 사용. 어댑터 포트를 직접 노출하지 않고 얇은 래퍼로 두어
 * 추후 필터(예: 접근성 기준 정렬, 장애유형별 필터) 를 캡슐화한다.
 */
public interface GetAccessiblePoiUseCase {

    List<AccessiblePoi> byArea(String areaCode, String contentTypeId, int limit);

    Optional<AccessiblePoi> detail(String contentId, String contentTypeId);

    List<AccessiblePoi> byLocation(double mapX, double mapY, int radius,
                                   String contentTypeId, int limit);

    List<AccessiblePoi> byKeyword(String keyword, String contentTypeId, int limit);

    boolean isConfigured();
}
