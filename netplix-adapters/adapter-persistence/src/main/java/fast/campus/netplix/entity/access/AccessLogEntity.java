package fast.campus.netplix.entity.access;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Entity
@Table(name = "access_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccessLogEntity {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ACCESS_LOG_ID")
    private Long accessLogId;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "PLATFORM")
    private String platform; // ANDROID, IOS, WEB, UNKNOWN

    @Column(name = "REQ_IP")
    private String reqIp;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    public AccessLogEntity(String userId, String platform, String reqIp) {
        this.userId = userId;
        this.platform = platform;
        this.reqIp = reqIp;
        this.createdAt = LocalDateTime.now(KST);
    }
}
