package fast.campus.netplix.repository.admin;

import fast.campus.netplix.entity.admin.AdminDailyStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface AdminDailyStatsJpaRepository extends JpaRepository<AdminDailyStatsEntity, LocalDate> {
}
