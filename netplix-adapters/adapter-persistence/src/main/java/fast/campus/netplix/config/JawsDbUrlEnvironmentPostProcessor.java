package fast.campus.netplix.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Heroku JawsDB/MySQL add-on: JAWSDB_URL 또는 DATABASE_URL이 있으면
 * DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD 로 파싱하여 주입.
 * 기존 DB_HOST 등이 이미 설정되어 있으면 덮어쓰지 않음.
 */
public class JawsDbUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROP_SOURCE_NAME = "jawsDbUrlProperties";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String url = environment.getProperty("JAWSDB_URL");
        if (url == null || url.isBlank()) {
            url = environment.getProperty("DATABASE_URL");
        }
        if (url == null || url.isBlank()) {
            return;
        }
        try {
            Map<String, Object> props = parseJdbcUrl(url);
            if (!props.isEmpty()) {
                PropertySource<?> ps = new MapPropertySource(PROP_SOURCE_NAME, props);
                environment.getPropertySources().addFirst(ps);
            }
        } catch (Exception e) {
            System.err.println("[JawsDbUrl] Failed to parse: " + e.getMessage());
        }
    }

    private Map<String, Object> parseJdbcUrl(String url) {
        Map<String, Object> props = new HashMap<>();
        String u = url.trim();
        if (u.startsWith("jdbc:")) {
            u = u.substring(5);
        }
        if (!u.startsWith("mysql://")) {
            return props;
        }
        try {
            URI uri = URI.create(u);
            String host = uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : 3306;
            String path = uri.getPath();
            String dbName = path != null && path.length() > 1 ? path.substring(1).split("\\?")[0] : "netplix";
            String userInfo = uri.getUserInfo();
            String username = "root";
            String password = "";
            if (userInfo != null && !userInfo.isBlank()) {
                int colon = userInfo.indexOf(':');
                if (colon >= 0) {
                    username = userInfo.substring(0, colon);
                    password = userInfo.substring(colon + 1);
                } else {
                    username = userInfo;
                }
            }
            if (host != null && !host.isBlank()) {
                // DataSource가 직접 사용하는 프로퍼티 (최우선)
                String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                        + "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul&useSSL=true&allowPublicKeyRetrieval=true";
                props.put("spring.datasource.hikari.jdbc-url", jdbcUrl);
                props.put("spring.datasource.hikari.username", username);
                props.put("spring.datasource.hikari.password", password);
                props.put("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
                // 기존 DB_* placeholder용
                props.put("DB_HOST", host);
                props.put("DB_PORT", String.valueOf(port));
                props.put("DB_NAME", dbName);
                props.put("DB_USERNAME", username);
                props.put("DB_PASSWORD", password);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid MySQL URL: " + url, e);
        }
        return props;
    }
}
