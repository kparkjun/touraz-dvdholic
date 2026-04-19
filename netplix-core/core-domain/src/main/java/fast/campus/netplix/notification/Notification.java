package fast.campus.netplix.notification;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class Notification {
    private final String notificationId;
    private final String userId;
    private final String title;
    private final String message;
    private final String notificationType;
    private final String relatedId;
    private Boolean isRead;
    private final LocalDateTime sentAt;

    public void markAsRead() {
        this.isRead = true;
    }

    public static Notification newRelease(String userId, String title, String message, String movieId) {
        return Notification.builder()
                .notificationId(UUID.randomUUID().toString())
                .userId(userId)
                .title(title)
                .message(message)
                .notificationType("NEW_RELEASE")
                .relatedId(movieId)
                .isRead(false)
                .sentAt(LocalDateTime.now())
                .build();
    }

    public static Notification newRecommendation(String userId, String title, String message, String movieId) {
        return Notification.builder()
                .notificationId(UUID.randomUUID().toString())
                .userId(userId)
                .title(title)
                .message(message)
                .notificationType("RECOMMENDATION")
                .relatedId(movieId)
                .isRead(false)
                .sentAt(LocalDateTime.now())
                .build();
    }

    public static Notification systemNotice(String userId, String title, String message) {
        return systemNotice(userId, title, message, null);
    }

    public static Notification systemNotice(String userId, String title, String message, String relatedId) {
        return Notification.builder()
                .notificationId(UUID.randomUUID().toString())
                .userId(userId)
                .title(title)
                .message(message)
                .notificationType("SYSTEM")
                .relatedId(relatedId)
                .isRead(false)
                .sentAt(LocalDateTime.now(java.time.ZoneId.of("Asia/Seoul")))
                .build();
    }
}
