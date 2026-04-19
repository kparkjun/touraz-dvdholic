package fast.campus.netplix.admin;

import fast.campus.netplix.entity.access.AccessLogEntity;
import fast.campus.netplix.entity.admin.AdminDailyStatsEntity;
import fast.campus.netplix.entity.admin.AdminEntity;
import fast.campus.netplix.entity.user.SocialUserEntity;
import fast.campus.netplix.entity.user.UserEntity;
import fast.campus.netplix.repository.access.AccessLogJpaRepository;
import fast.campus.netplix.repository.admin.AdminDailyStatsJpaRepository;
import fast.campus.netplix.repository.admin.AdminJpaRepository;
import fast.campus.netplix.repository.user.SocialUserJpaRepository;
import fast.campus.netplix.repository.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AdminPersistenceAdapter implements AdminAuthPort, AdminDashboardPort, InsertAccessLogPort {

    private final AdminJpaRepository adminJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final SocialUserJpaRepository socialUserJpaRepository;
    private final AccessLogJpaRepository accessLogJpaRepository;
    private final AdminDailyStatsJpaRepository adminDailyStatsJpaRepository;

    @Override
    public Optional<AdminAuthInfo> findByAdminIdOrEmail(String idOrEmail) {
        return adminJpaRepository.findByAdminIdOrAdminEmail(idOrEmail, idOrEmail)
                .map(a -> new AdminAuthInfo(a.getAdminId(), a.getPassword()));
    }

    @Override
    public List<AdminInfo> findAllAdmins() {
        return adminJpaRepository.findAll().stream()
                .map(a -> new AdminInfo(a.getAdminId(), a.getAdminEmail(), a.getAdminName(), a.getRole()))
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminUserInfo> findAllUsers() {
        List<AdminUserInfo> result = new ArrayList<>();

        List<UserEntity> users = userJpaRepository.findAll();
        for (UserEntity u : users) {
            result.add(new AdminUserInfo(
                    u.getUserId(), u.getUsername(), u.getEmail(), u.getPhone(), u.getStatus(),
                    u.getWithdrawnAt(), u.getLastLoginAt(), "email"));
        }

        List<SocialUserEntity> socialUsers = socialUserJpaRepository.findAll();
        for (SocialUserEntity s : socialUsers) {
            result.add(new AdminUserInfo(
                    s.getSocialUserId(), s.getUsername(), null, null, "ACTIVE",
                    null, null, s.getProvider()));
        }

        return result;
    }

    @Override
    public void insertAccessLog(String userId, String platform, String reqIp) {
        accessLogJpaRepository.save(new AccessLogEntity(userId, platform, reqIp));
    }

    @Override
    public List<AccessLogInfo> findAllAccessLogs() {
        return accessLogJpaRepository.findAllDesc().stream()
                .map(a -> new AccessLogInfo(a.getAccessLogId(), a.getUserId(), a.getPlatform(), a.getReqIp(), a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public List<DailyStatsInfo> findAllDailyStats() {
        List<Object[]> rows = accessLogJpaRepository.aggregateDailyStats();
        List<DailyStatsInfo> result = new ArrayList<>();
        for (Object[] row : rows) {
            LocalDate statDate = row[0] instanceof Date d ? d.toLocalDate() : LocalDate.parse(row[0].toString());
            int active = toInt(row[1]);
            int total = toInt(row[2]);
            int android = toInt(row[3]);
            int ios = toInt(row[4]);
            int web = toInt(row[5]);
            int other = toInt(row[6]);
            int newSignup = toInt(row[7]);
            result.add(new DailyStatsInfo(statDate, active, total, android, ios, web, other, newSignup, LocalDateTime.now()));
        }
        return result;
    }

    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number n) return n.intValue();
        return Integer.parseInt(value.toString());
    }
}
