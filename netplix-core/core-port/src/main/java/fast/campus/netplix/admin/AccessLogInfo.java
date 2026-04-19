package fast.campus.netplix.admin;

import java.time.LocalDateTime;

public record AccessLogInfo(Long accessLogId, String userId, String platform, String reqIp, LocalDateTime createdAt) {}
