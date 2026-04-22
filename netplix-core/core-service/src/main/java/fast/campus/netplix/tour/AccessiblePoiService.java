package fast.campus.netplix.tour;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 한국관광공사 무장애 여행정보 조회 서비스.
 * 캐시는 어댑터({@code VisitKoreaAccessibleHttpClient}) 내부에서 관리하므로 위임 중심.
 * 다만 limit 바운더리는 서비스에서 통제한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccessiblePoiService implements GetAccessiblePoiUseCase {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 30;

    private final AccessiblePoiPort port;

    @Override
    public List<AccessiblePoi> byArea(String areaCode, String contentTypeId, int limit) {
        return port.fetchByArea(areaCode, contentTypeId, sanitize(limit));
    }

    @Override
    public Optional<AccessiblePoi> detail(String contentId, String contentTypeId) {
        return port.fetchDetail(contentId, contentTypeId);
    }

    @Override
    public List<AccessiblePoi> byLocation(double mapX, double mapY, int radius,
                                          String contentTypeId, int limit) {
        return port.fetchByLocation(mapX, mapY, radius, contentTypeId, sanitize(limit));
    }

    @Override
    public List<AccessiblePoi> byKeyword(String keyword, String contentTypeId, int limit) {
        return port.fetchByKeyword(keyword, contentTypeId, sanitize(limit));
    }

    @Override
    public boolean isConfigured() {
        return port.isConfigured();
    }

    private int sanitize(int limit) {
        if (limit <= 0) return DEFAULT_LIMIT;
        return Math.min(limit, MAX_LIMIT);
    }
}
