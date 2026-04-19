package fast.campus.netplix.repository.subscription;

import fast.campus.netplix.entity.subscription.UserSubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserSubscriptionJpaRepository extends JpaRepository<UserSubscriptionEntity, String>, UserSubscriptionCustomRepository {

    @Modifying
    @Query("DELETE FROM UserSubscriptionEntity u WHERE u.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);
}
