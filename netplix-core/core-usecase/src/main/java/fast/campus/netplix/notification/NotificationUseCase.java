package fast.campus.netplix.notification;

import java.util.List;

public interface NotificationUseCase {
    List<Notification> getNotifications(String userId);
    
    List<Notification> getUnreadNotifications(String userId);
    
    long getUnreadCount(String userId);
    
    void markAsRead(String notificationId);
    
    void markAllAsRead(String userId);
    
    void sendNewReleaseNotification(String movieName, String movieId);

    void sendRecommendationNotification(String userId, String movieName, String movieId);

    /**
     * 배치(영화/DVD 목록 업데이트) 완료 시 전체 사용자에게 시스템 알림 1건 발송.
     */
    void sendBatchUpdateNotification(String title, String message);

    void sendBatchUpdateNotification(String title, String message, String relatedData);

    void deleteAllNotifications();

    int deleteOldNotifications(int days);
}
