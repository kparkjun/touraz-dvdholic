package fast.campus.netplix.notification;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationPort {
    int deleteOlderThan(LocalDateTime cutoff);
    Notification save(Notification notification);
    
    List<Notification> findByUserId(String userId);
    
    List<Notification> findUnreadByUserId(String userId);
    
    long countUnreadByUserId(String userId);
    
    void markAsRead(String notificationId);
    
    void markAllAsReadByUserId(String userId);
    
    void deleteByNotificationId(String notificationId);

    void deleteAll();
}
