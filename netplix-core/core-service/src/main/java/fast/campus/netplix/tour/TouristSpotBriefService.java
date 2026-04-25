package fast.campus.netplix.tour;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 관광지 단건 "간단 상세" 조회 서비스. 어댑터(폴백 체인 + 캐시)에 위임만 담당.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TouristSpotBriefService implements GetTouristSpotBriefUseCase {

    private final TouristSpotBriefPort port;

    @Override
    public Optional<TouristSpotBrief> findFirst(String keyword, String bjdAreaCode) {
        if (keyword == null || keyword.isBlank()) return Optional.empty();
        return port.findFirstByKeyword(keyword, bjdAreaCode);
    }

    @Override
    public boolean isConfigured() {
        return port.isConfigured();
    }
}
