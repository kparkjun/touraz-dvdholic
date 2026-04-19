package fast.campus.netplix.admin;

import java.time.LocalDateTime;

public record AdminUserInfo(
        String userId, String username, String email, String phone, String status,
        LocalDateTime withdrawnAt, LocalDateTime lastLoginAt, String provider) {}
