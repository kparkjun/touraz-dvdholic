package fast.campus.netplix.tour;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 한국관광공사 TarRlteTarService1 조회 서비스.
 *
 * <p>캐시는 어댑터에서 관리하고, 서비스는 limit sanitization 과 위임만 담당한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RelatedTouristSpotService implements GetRelatedTouristSpotUseCase {

    private static final int DEFAULT_LIMIT = Integer.MAX_VALUE;

    private final RelatedTouristSpotPort port;

    @Override
    public List<RelatedTouristSpot> byKeyword(String keyword, int limit) {
        return port.fetchByKeyword(keyword, sanitize(limit));
    }

    @Override
    public List<RelatedTouristSpot> byArea(String areaCode, String signguCode, int limit) {
        return port.fetchByArea(areaCode, signguCode, sanitize(limit));
    }

    @Override
    public boolean isConfigured() {
        return port.isConfigured();
    }

    private int sanitize(int limit) {
        return limit <= 0 ? DEFAULT_LIMIT : limit;
    }
}
