package fast.campus.netplix.admin;

import java.util.List;

public interface AdminDashboardUseCase {
    List<AdminInfo> getAdmins();
    List<AdminUserInfo> getUsers();
    List<AccessLogInfo> getAccessLogs();
    List<DailyStatsInfo> getDailyStats();
    void logAccess(String userId, String userAgent, String reqIp);
}
