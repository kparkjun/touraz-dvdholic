package fast.campus.netplix.admin;

import java.util.List;

public interface AdminDashboardPort {
    List<AdminInfo> findAllAdmins();
    List<AdminUserInfo> findAllUsers();
    List<AccessLogInfo> findAllAccessLogs();
    List<DailyStatsInfo> findAllDailyStats();
}
