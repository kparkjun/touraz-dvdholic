package fast.campus.netplix.notification;

import fast.campus.netplix.user.SearchUserPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService implements NotificationUseCase {

    private final NotificationPort notificationPort;
    private final SearchUserPort searchUserPort;

    @Override
    public List<Notification> getNotifications(String userId) {
        return notificationPort.findByUserId(userId);
    }

    @Override
    public List<Notification> getUnreadNotifications(String userId) {
        return notificationPort.findUnreadByUserId(userId);
    }

    @Override
    public long getUnreadCount(String userId) {
        return notificationPort.countUnreadByUserId(userId);
    }

    @Override
    public void markAsRead(String notificationId) {
        notificationPort.markAsRead(notificationId);
    }

    @Override
    public void markAllAsRead(String userId) {
        notificationPort.markAllAsReadByUserId(userId);
    }

    @Override
    public void sendNewReleaseNotification(String movieName, String movieId) {
        List<String> allUserIds = searchUserPort.findAllUserIds();
        log.info("Sending new release notification for '{}' to {} users", movieName, allUserIds.size());
        
        for (String userId : allUserIds) {
            Notification notification = Notification.newRelease(
                    userId,
                    "새 DVD 출시!",
                    "'" + movieName + "'이(가) 새로 출시되었습니다. 지금 확인해 보세요!",
                    movieId
            );
            notificationPort.save(notification);
        }
    }

    @Override
    public void sendRecommendationNotification(String userId, String movieName, String movieId) {
        Notification notification = Notification.newRecommendation(
                userId,
                "추천 영화",
                "취향에 맞는 '" + movieName + "'을(를) 추천드려요!",
                movieId
        );
        notificationPort.save(notification);
    }

    @Override
    public void sendBatchUpdateNotification(String title, String message) {
        sendBatchUpdateNotification(title, message, null);
    }

    @Override
    public void sendBatchUpdateNotification(String title, String message, String relatedData) {
        List<String> allUserIds = searchUserPort.findAllUserIds();
        if (allUserIds.isEmpty()) {
            log.warn("[BATCH-NOTI] 알림 발송 대상 사용자 없음");
            return;
        }
        log.info("[BATCH-NOTI] 발송 시작: title='{}', message='{}', relatedData길이={}, 대상={}명",
                title, message, relatedData != null ? relatedData.length() : 0, allUserIds.size());
        int success = 0;
        for (String userId : allUserIds) {
            try {
                Notification notification = Notification.systemNotice(userId, title, message, relatedData);
                notificationPort.save(notification);
                success++;
            } catch (Exception e) {
                log.error("[BATCH-NOTI] userId={} 알림 저장 실패: {}", userId, e.getMessage());
            }
        }
        log.info("[BATCH-NOTI] 발송 완료: {}/{}명 성공", success, allUserIds.size());
    }

    @Override
    public void deleteAllNotifications() {
        notificationPort.deleteAll();
    }

    @Override
    public int deleteOldNotifications(int days) {
        LocalDateTime cutoff = LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusDays(days);
        int deleted = notificationPort.deleteOlderThan(cutoff);
        if (deleted > 0) {
            log.info("[NOTI-CLEANUP] {}일 지난 알림 {}건 삭제", days, deleted);
        }
        return deleted;
    }
}
