package fast.campus.netplix;

import fast.campus.netplix.migration.MigrateApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NetplixApiApplication {
    public static void main(String[] args) {
        String profile = System.getProperty("spring.profiles.active");
        if (profile == null) profile = System.getenv("SPRING_PROFILES_ACTIVE");
        if (profile != null && profile.contains("migrate")) {
            MigrateApplication.main(args);
            return;  // MigrateApplication.main() calls System.exit(0)
        }
        SpringApplication.run(NetplixApiApplication.class, args);
    }
}
