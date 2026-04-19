package fast.campus.netplix.migration;

import org.flywaydb.core.Flyway;

import java.net.URI;

/**
 * Spring 없이 Flyway 마이그레이션만 실행.
 * Heroku release phase에서 실행: JPA/Spring 로드 없이 DB 접속 후 Flyway만 수행 후 종료.
 */
public class MigrateApplication {

    public static void main(String[] args) {
        JdbcInfo info = getJdbcInfo();
        if (info == null) {
            System.err.println("[Migrate] DB URL not found. Set JAWSDB_URL, DATABASE_URL, or DB_HOST/DB_PORT/DB_NAME.");
            System.exit(1);
        }

        Flyway flyway = Flyway.configure()
                .dataSource(info.jdbcUrl, info.username, info.password)
                .locations("classpath:flyway")
                .baselineOnMigrate(true)
                .load();

        if ("true".equalsIgnoreCase(System.getenv("FLYWAY_REPAIR"))) {
            flyway.repair();
        }
        flyway.migrate();
        System.out.println("[Migrate] Flyway migration completed.");
        System.exit(0);
    }

    private static JdbcInfo getJdbcInfo() {
        String url = getEnv("JAWSDB_URL", null);
        if (url == null) url = getEnv("DATABASE_URL", null);
        if (url != null && !url.isBlank()) {
            return parseFromUrl(url);
        }
        String host = getEnv("DB_HOST", "localhost");
        String port = getEnv("DB_PORT", "3306");
        String dbName = getEnv("DB_NAME", "netplix");
        String useSsl = getEnv("DB_USE_SSL", "false");
        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                + "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul&useSSL=" + useSsl + "&allowPublicKeyRetrieval=true";
        return new JdbcInfo(jdbcUrl, getEnv("DB_USERNAME", "root"), getEnv("DB_PASSWORD", ""));
    }

    private static JdbcInfo parseFromUrl(String url) {
        String u = url.trim();
        if (u.startsWith("jdbc:")) u = u.substring(5);
        if (!u.startsWith("mysql://")) return null;
        try {
            URI uri = URI.create(u);
            String host = uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : 3306;
            String path = uri.getPath();
            String dbName = (path != null && path.length() > 1) ? path.substring(1).split("\\?")[0] : "netplix";
            String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                    + "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul&useSSL=true&allowPublicKeyRetrieval=true";
            String username = "root", password = "";
            String userInfo = uri.getUserInfo();
            if (userInfo != null && !userInfo.isBlank()) {
                int colon = userInfo.indexOf(':');
                username = colon >= 0 ? userInfo.substring(0, colon) : userInfo;
                password = colon >= 0 ? userInfo.substring(colon + 1) : "";
            }
            return new JdbcInfo(jdbcUrl, username, password);
        } catch (Exception e) {
            System.err.println("[Migrate] Failed to parse URL: " + e.getMessage());
            return null;
        }
    }

    private record JdbcInfo(String jdbcUrl, String username, String password) {}

    private static String getEnv(String key, String fallback) {
        String v = System.getenv(key);
        return (v != null && !v.isBlank()) ? v : fallback;
    }
}
