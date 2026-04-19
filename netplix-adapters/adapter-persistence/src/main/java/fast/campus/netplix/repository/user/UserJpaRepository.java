package fast.campus.netplix.repository.user;

import fast.campus.netplix.entity.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface UserJpaRepository extends JpaRepository<UserEntity, String>, UserCustomRepository {

    @Modifying
    @Query("UPDATE UserEntity u SET u.lastLoginAt = :now WHERE u.email = :email")
    void updateLastLoginAtByEmail(@Param("email") String email, @Param("now") LocalDateTime now);
}
