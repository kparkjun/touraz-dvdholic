package fast.campus.netplix.admin;

import java.util.Optional;

public interface AdminAuthPort {
    Optional<AdminAuthInfo> findByAdminIdOrEmail(String idOrEmail);
}
