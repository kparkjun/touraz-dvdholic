package fast.campus.netplix.controller.admin;

import fast.campus.netplix.admin.*;
import fast.campus.netplix.controller.NetplixApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminLoginUseCase adminLoginUseCase;
    private final AdminDashboardUseCase adminDashboardUseCase;

    @PostMapping("/login")
    public NetplixApiResponse<Map<String, String>> login(@RequestBody AdminLoginRequest request) {
        AdminLoginResult result = adminLoginUseCase.login(request.adminId(), request.password());
        return NetplixApiResponse.ok(Map.of("token", result.token()));
    }

    @GetMapping("/admins")
    public NetplixApiResponse<List<AdminInfo>> getAdmins() {
        return NetplixApiResponse.ok(adminDashboardUseCase.getAdmins());
    }

    @GetMapping("/users")
    public NetplixApiResponse<List<AdminUserInfo>> getUsers() {
        return NetplixApiResponse.ok(adminDashboardUseCase.getUsers());
    }

    @GetMapping("/access-logs")
    public NetplixApiResponse<List<AccessLogInfo>> getAccessLogs() {
        return NetplixApiResponse.ok(adminDashboardUseCase.getAccessLogs());
    }

    @GetMapping("/daily-stats")
    public NetplixApiResponse<List<DailyStatsInfo>> getDailyStats() {
        return NetplixApiResponse.ok(adminDashboardUseCase.getDailyStats());
    }

    public record AdminLoginRequest(String adminId, String password) {}
}
