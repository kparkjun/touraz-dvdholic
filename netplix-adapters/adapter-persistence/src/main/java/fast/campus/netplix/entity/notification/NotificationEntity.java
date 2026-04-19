package fast.campus.netplix.entity.notification;

import fast.campus.netplix.entity.audit.MutableBaseEntity;
import fast.campus.netplix.notification.Notification;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class NotificationEntity extends MutableBaseEntity {
    @Id
    @Column(name = "NOTIFICATION_ID")
    private String notificationId;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "MESSAGE", length = 1000)
    private String message;

    @Column(name = "NOTIFICATION_TYPE")
    private String notificationType;

    @Column(name = "RELATED_ID", columnDefinition = "MEDIUMTEXT")
    private String relatedId;

    @Column(name = "IS_READ")
    private Boolean isRead;

    @Column(name = "SENT_AT")
    private LocalDateTime sentAt;

    public Notification toDomain() {
        return Notification.builder()
                .notificationId(notificationId)
                .userId(userId)
                .title(title)
                .message(message)
                .notificationType(notificationType)
                .relatedId(relatedId)
                .isRead(isRead)
                .sentAt(sentAt)
                .build();
    }

    public static NotificationEntity toEntity(Notification domain) {
        return new NotificationEntity(
                domain.getNotificationId(),
                domain.getUserId(),
                domain.getTitle(),
                domain.getMessage(),
                domain.getNotificationType(),
                domain.getRelatedId(),
                domain.getIsRead(),
                domain.getSentAt()
        );
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
