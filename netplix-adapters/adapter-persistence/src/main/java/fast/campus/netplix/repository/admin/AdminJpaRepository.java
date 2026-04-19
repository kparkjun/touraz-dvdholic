package fast.campus.netplix.repository.admin;

import fast.campus.netplix.entity.admin.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminJpaRepository extends JpaRepository<AdminEntity, String> {
    Optional<AdminEntity> findByAdminIdOrAdminEmail(String adminId, String adminEmail);
}
