package fast.campus.netplix.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService implements AdminLoginUseCase, AdminDashboardUseCase {

    private final AdminAuthPort adminAuthPort;
    private final AdminDashboardPort adminDashboardPort;
    private final InsertAccessLogPort insertAccessLogPort;

    private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();
    private static final long TOKEN_EXPIRE_MS = 24 * 60 * 60 * 1000L;
    private final Map<String, TokenEntry> adminTokens = new ConcurrentHashMap<>();

    @Override
    public AdminLoginResult login(String adminId, String password) {
        log.info("[Admin Login] adminId='{}', password length={}", adminId, password != null ? password.length() : 0);
        Optional<AdminAuthInfo> opt = adminAuthPort.findByAdminIdOrEmail(adminId);
        log.info("[Admin Login] DB lookup result: present={}", opt.isPresent());
        if (opt.isEmpty()) {
            log.warn("[Admin Login] admin not found for id='{}'", adminId);
            throw new RuntimeException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        AdminAuthInfo info = opt.get();
        boolean passwordMatch = BCRYPT.matches(password, info.hashedPassword());
        log.info("[Admin Login] password match={}, hash prefix={}", passwordMatch,
                info.hashedPassword() != null ? info.hashedPassword().substring(0, Math.min(10, info.hashedPassword().length())) : "null");
        if (!passwordMatch) {
            throw new RuntimeException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        String token = UUID.randomUUID().toString();
        adminTokens.put(token, new TokenEntry(info.adminId(), System.currentTimeMillis() + TOKEN_EXPIRE_MS));
        return new AdminLoginResult(token);
    }

    @Override
    public Optional<String> validateToken(String token) {
        TokenEntry entry = adminTokens.get(token);
        if (entry == null || System.currentTimeMillis() > entry.expiresAt()) {
            if (entry != null) adminTokens.remove(token);
            return Optional.empty();
        }
        return Optional.of(entry.adminId());
    }

    @Override
    public List<AdminInfo> getAdmins() {
        return adminDashboardPort.findAllAdmins();
    }

    @Override
    public List<AdminUserInfo> getUsers() {
        return adminDashboardPort.findAllUsers();
    }

    @Override
    public List<AccessLogInfo> getAccessLogs() {
        return adminDashboardPort.findAllAccessLogs();
    }

    @Override
    public List<DailyStatsInfo> getDailyStats() {
        return adminDashboardPort.findAllDailyStats();
    }

    @Override
    public void logAccess(String userId, String userAgent, String reqIp) {
        String platform = detectPlatform(userAgent);
        insertAccessLogPort.insertAccessLog(userId, platform, reqIp);
    }

    private String detectPlatform(String userAgent) {
        if (userAgent == null) return "UNKNOWN";
        String ua = userAgent.toLowerCase();
        if (ua.contains("android")) return "ANDROID";
        if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ios")) return "IOS";
        if (ua.contains("mozilla") || ua.contains("chrome") || ua.contains("safari") || ua.contains("firefox")) return "WEB";
        return "UNKNOWN";
    }

    private record TokenEntry(String adminId, long expiresAt) {}
}
