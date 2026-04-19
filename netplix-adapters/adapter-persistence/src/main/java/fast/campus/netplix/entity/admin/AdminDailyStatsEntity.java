package fast.campus.netplix.entity.admin;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "admin_daily_stats")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminDailyStatsEntity {
    @Id
    @Column(name = "STAT_DATE")
    private LocalDate statDate;

    @Column(name = "ACTIVE_USER_COUNT")
    private int activeUserCount;

    @Column(name = "TOTAL_USER_COUNT")
    private int totalUserCount;

    @Column(name = "ANDROID_COUNT")
    private int androidCount;

    @Column(name = "IOS_COUNT")
    private int iosCount;

    @Column(name = "WEB_COUNT")
    private int webCount;

    @Column(name = "OTHER_COUNT")
    private int otherCount;

    @Column(name = "NEW_SIGNUP_COUNT")
    private int newSignupCount;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    public AdminDailyStatsEntity(
            LocalDate statDate,
            int activeUserCount,
            int totalUserCount,
            int androidCount,
            int iosCount,
            int webCount,
            int otherCount,
            int newSignupCount
    ) {
        this.statDate = statDate;
        this.activeUserCount = activeUserCount;
        this.totalUserCount = totalUserCount;
        this.androidCount = androidCount;
        this.iosCount = iosCount;
        this.webCount = webCount;
        this.otherCount = otherCount;
        this.newSignupCount = newSignupCount;
        this.createdAt = LocalDateTime.now();
    }
}
