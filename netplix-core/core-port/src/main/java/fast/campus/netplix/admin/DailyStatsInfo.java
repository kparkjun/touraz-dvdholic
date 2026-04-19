package fast.campus.netplix.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DailyStatsInfo(
        LocalDate statDate, int activeUserCount, int totalUserCount,
        int androidCount, int iosCount, int webCount, int otherCount, int newSignupCount,
        LocalDateTime createdAt) {}
