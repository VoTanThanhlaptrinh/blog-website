package com.blog.backend.notification.application.listener;

import com.blog.backend.admin.domain.enums.PenaltyAction;
import com.blog.backend.admin.domain.event.UserPenalizedEvent;
import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.notification.application.NotificationService;
import com.blog.backend.notification.application.service.MailService;
import com.blog.backend.notification.domain.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserPenalizedEventListener {

    private final NotificationService notificationService;
    private final MailService mailService;

    @Async
    @EventListener
    public void handleUserPenalizedEvent(UserPenalizedEvent event) {
        User user = event.getUser();
        if (user == null) return;

        String title;
        String content;

        if (event.getAction() == PenaltyAction.LOCK) {
            title = "Tài khoản của bạn đã bị khóa";
            content = String.format("Tài khoản của bạn đã bị khóa do vi phạm tiêu chuẩn cộng đồng. Lý do: %s", event.getReason());
        } else {
            if (event.getCurrentWarningCount() >= 3) {
                title = "Tài khoản bị khóa do vượt quá số lần cảnh cáo";
                content = String.format("Bạn đã nhận %d lần cảnh cáo (%s). Tài khoản của bạn hiện đã bị khóa.",
                        event.getCurrentWarningCount(), event.getReason());
            } else {
                title = "Thông báo cảnh cáo tài khoản";
                content = String.format("Tài khoản của bạn đã nhận 1 cảnh cáo từ Quản trị viên (Tổng cộng: %d/3). Lý do: %s",
                        event.getCurrentWarningCount(), event.getReason());
            }
        }

        // Create In-App Notification
        notificationService.createNotification(
                user,
                title,
                content,
                NotificationType.SYSTEM,
                "/profile"
        );

        // Send Email
        try {
            mailService.sendMail(user.getEmail(), title, content);
        } catch (Exception e) {
            log.error("Failed to send penalty email to {}: {}", user.getEmail(), e.getMessage());
        }
    }
}
