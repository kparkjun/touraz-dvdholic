package fast.campus.netplix.tour;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 한국관광공사 반려동물 동반여행(KorPetTourService) 조회 서비스.
 * 캐시는 어댑터 내부에서 관리, 서비스는 limit 바운더리 + 위임만 담당.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PetFriendlyPoiService implements GetPetFriendlyPoiUseCase {

    // limit <= 0 이면 어댑터가 모은 전체 건수를 그대로 반환(상한 없음).
    // 호출자가 양수를 주면 그 값으로 잘라 반환.
    private static final int DEFAULT_LIMIT = Integer.MAX_VALUE;

    private final PetFriendlyPoiPort port;

    @Override
    public List<PetFriendlyPoi> byArea(String areaCode, String contentTypeId, int limit) {
        return port.fetchByArea(areaCode, contentTypeId, sanitize(limit));
    }

    @Override
    public Optional<PetFriendlyPoi> detail(String contentId, String contentTypeId) {
        return port.fetchDetail(contentId, contentTypeId);
    }

    @Override
    public List<PetFriendlyPoi> byLocation(double mapX, double mapY, int radius,
                                           String contentTypeId, int limit) {
        return port.fetchByLocation(mapX, mapY, radius, contentTypeId, sanitize(limit));
    }

    @Override
    public List<PetFriendlyPoi> byKeyword(String keyword, String contentTypeId, int limit) {
        return port.fetchByKeyword(keyword, contentTypeId, sanitize(limit));
    }

    @Override
    public boolean isConfigured() {
        return port.isConfigured();
    }

    private int sanitize(int limit) {
        if (limit <= 0) return DEFAULT_LIMIT;
        return limit;
    }
}
