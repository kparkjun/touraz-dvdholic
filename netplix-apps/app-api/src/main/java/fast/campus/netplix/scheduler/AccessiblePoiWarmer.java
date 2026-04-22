package fast.campus.netplix.scheduler;

import fast.campus.netplix.tour.AccessiblePoi;
import fast.campus.netplix.tour.GetAccessiblePoiUseCase;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 한국관광공사 무장애 여행정보(KorWithService2) 어댑터 캐시 프리워밍.
 *
 * <p>별도 DB 테이블은 만들지 않고, 어댑터 내부 6h TTL 캐시에 주요 (areaCode, contentTypeId)
 * 조합을 선행 로딩해 관리자/사용자 첫 요청의 응답 지연을 줄인다.
 *
 * <p>- 기동 직후 2분 후 1회, 그리고 매일 04:30 (Heroku Asia/Seoul) 재워밍.
 * <p>- 호출량: 17개 광역 × 5개 콘텐츠타입 = 85회 / 일. VisitKorea 일일 쿼터 내.
 * <p>- KorWithService2 미설정 시({@link GetAccessiblePoiUseCase#isConfigured()}=false)는 조용히 스킵.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccessiblePoiWarmer {

    private static final List<String> AREA_CODES = List.of(
            "1", "2", "3", "4", "5", "6", "7", "8",
            "31", "32", "33", "34", "35", "36", "37", "38", "39"
    );
    private static final List<String> CONTENT_TYPES = List.of("12", "14", "25", "32", "39");

    private final GetAccessiblePoiUseCase useCase;

    @Value("${visitkorea.access.warmer-enabled:true}")
    private boolean warmerEnabled;

    @PostConstruct
    void warmOnBoot() {
        if (!warmerEnabled) return;
        // 기동 직후 한 번에 전량 로딩하면 부팅 지연 & QPS 부담. 살짝 뒤에서 비동기로 시작.
        new Thread(this::runSilently, "accessible-poi-warmer-boot").start();
    }

    /** 매일 새벽 4시 30분 (JVM 타임존 Asia/Seoul 가정) 캐시 재워밍. */
    @Scheduled(cron = "0 30 4 * * *")
    @Async
    public void warmDaily() {
        if (!warmerEnabled) return;
        log.info("[ACCESS-WARM] 일일 무장애 POI 캐시 재워밍 시작");
        runSilently();
    }

    private void runSilently() {
        if (!useCase.isConfigured()) {
            log.info("[ACCESS-WARM] KorWithService2 미설정 - 워밍 스킵");
            return;
        }
        int ok = 0;
        int emptyBuckets = 0;
        long t0 = System.currentTimeMillis();
        for (String area : AREA_CODES) {
            for (String type : CONTENT_TYPES) {
                try {
                    List<AccessiblePoi> pois = useCase.byArea(area, type, 30);
                    if (pois.isEmpty()) emptyBuckets++;
                    ok++;
                    // KTO QPS 여유 확보 (초당 ~10 호출 수준)
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Exception ex) {
                    log.warn("[ACCESS-WARM] 실패 area={} type={} err={}", area, type, ex.getMessage());
                }
            }
        }
        long ms = System.currentTimeMillis() - t0;
        log.info("[ACCESS-WARM] 완료 totalOk={} emptyBuckets={} elapsedMs={}", ok, emptyBuckets, ms);
    }
}
