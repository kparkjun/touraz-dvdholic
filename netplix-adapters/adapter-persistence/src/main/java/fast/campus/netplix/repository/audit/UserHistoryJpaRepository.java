package fast.campus.netplix.repository.audit;

import fast.campus.netplix.entity.user.history.UserHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserHistoryJpaRepository extends JpaRepository<UserHistoryEntity, Long> {

    @Modifying
    @Query("DELETE FROM UserHistoryEntity u WHERE u.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);
}
