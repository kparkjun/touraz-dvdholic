package fast.campus.netplix.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Flyway 실패/체크섬 불일치 시: flyway.repair=true 또는 FLYWAY_REPAIR=true 일 때 시작 전 repair 실행.
 * (로컬에서 마이그레이션 파일 수정 후 체크섬 불일치 시 한 번만 켜고 재시작)
 */
@Configuration
public class FlywayConfig {

    @Value("${flyway.repair:false}")
    private boolean flywayRepair;

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            boolean doRepair = flywayRepair || "true".equalsIgnoreCase(System.getenv("FLYWAY_REPAIR"));
            if (doRepair) {
                flyway.repair();
            }
            flyway.migrate();
        };
    }
}
