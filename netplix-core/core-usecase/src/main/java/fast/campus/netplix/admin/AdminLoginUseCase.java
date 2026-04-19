package fast.campus.netplix.admin;

import java.util.Optional;

public interface AdminLoginUseCase {
    AdminLoginResult login(String adminId, String password);
    Optional<String> validateToken(String token);
}
