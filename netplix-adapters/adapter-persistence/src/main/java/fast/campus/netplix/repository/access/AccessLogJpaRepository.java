package fast.campus.netplix.repository.access;

import fast.campus.netplix.entity.access.AccessLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AccessLogJpaRepository extends JpaRepository<AccessLogEntity, Long> {

    @Query(value =
            "SELECT DATE(a.CREATED_AT) AS statDate, " +
            "  COUNT(DISTINCT a.USER_ID) AS activeUserCount, " +
            "  (SELECT COUNT(*) FROM users) AS totalUserCount, " +
            "  SUM(CASE WHEN a.PLATFORM = 'ANDROID' THEN 1 ELSE 0 END) AS androidCount, " +
            "  SUM(CASE WHEN a.PLATFORM = 'IOS' THEN 1 ELSE 0 END) AS iosCount, " +
            "  SUM(CASE WHEN a.PLATFORM = 'WEB' THEN 1 ELSE 0 END) AS webCount, " +
            "  SUM(CASE WHEN a.PLATFORM NOT IN ('ANDROID','IOS','WEB') THEN 1 ELSE 0 END) AS otherCount, " +
            "  (SELECT COUNT(*) FROM users u WHERE DATE(u.CREATED_AT) = DATE(a.CREATED_AT)) AS newSignupCount " +
            "FROM access_logs a " +
            "GROUP BY DATE(a.CREATED_AT) " +
            "ORDER BY statDate DESC " +
            "LIMIT 30",
            nativeQuery = true)
    List<Object[]> aggregateDailyStats();

    @Query(value = "SELECT * FROM access_logs ORDER BY CREATED_AT DESC", nativeQuery = true)
    List<AccessLogEntity> findAllDesc();
}
