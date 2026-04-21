package fast.campus.netplix.cinetrip;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 기동 시 classpath 의 cine-trip-seed.csv 를 읽어 매핑 테이블에 delta upsert 한다.
 * - (movieName, areaCode, mappingType) 유니크 키 기반 idempotent upsert
 * - 새 행 추가 시 배포만으로 자동 반영, 기존 행은 값만 갱신
 * - 공모전 데모용 시드. 실제 운영에서는 관리자 /import 엔드포인트 병행 사용.
 */
@Slf4j
@Component
@Order(110)
@RequiredArgsConstructor
public class CineTripSeedLoader implements ApplicationRunner {

    private static final String SEED_PATH = "cine-trip-seed.csv";

    private final CineTripUseCase cineTripUseCase;

    @Override
    public void run(ApplicationArguments args) {
        try {
            ClassPathResource resource = new ClassPathResource(SEED_PATH);
            if (!resource.exists()) {
                log.info("[CINE-TRIP-SEED] {} 없음 — 생략", SEED_PATH);
                return;
            }
            long before = cineTripUseCase.count();
            String csv;
            try (var is = resource.getInputStream()) {
                csv = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
            int imported = cineTripUseCase.importFromCsv(csv);
            long after = cineTripUseCase.count();
            long delta = after - before;
            if (delta > 0) {
                log.info("[CINE-TRIP-SEED] upsert 완료: 처리 {}행, 신규 {}건 (총 {}건)", imported, delta, after);
            } else {
                log.info("[CINE-TRIP-SEED] upsert 완료: 처리 {}행, 신규 없음 (총 {}건)", imported, after);
            }
        } catch (Exception e) {
            log.warn("[CINE-TRIP-SEED] 시드 적재 실패(무시): {}", e.getMessage());
        }
    }
}
