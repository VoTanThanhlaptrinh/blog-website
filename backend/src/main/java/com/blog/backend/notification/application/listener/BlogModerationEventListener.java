package com.blog.backend.notification.application.listener;

import com.blog.backend.content.domain.entity.Blog;
import com.blog.backend.content.domain.enums.BlogStatus;
import com.blog.backend.content.domain.event.BlogModerationEvent;
import com.blog.backend.notification.application.service.MailService;
import com.blog.backend.notification.application.NotificationService;
import com.blog.backend.notification.domain.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BlogModerationEventListener {

    private final NotificationService notificationService;
    private final MailService mailService;

    @Async
    @EventListener
    public void handleBlogModerationEvent(BlogModerationEvent event) {
        Blog blog = event.getBlog();
        if (blog == null || blog.getUser() == null) return;

        String title;
        String content;

        if (event.getNewStatus() == BlogStatus.PUBLISHED) {
            title = "Bài viết đã được phê duyệt!";
            content = String.format("Chúc mừng! Bài viết '%s' của bạn đã được Admin phê duyệt và xuất bản.", blog.getTitle());
        } else if (event.getNewStatus() == BlogStatus.REJECTED) {
            title = "Bài viết bị từ chối xuất bản";
            content = String.format("Bài viết '%s' của bạn đã bị từ chối. Lý do: %s", 
                    blog.getTitle(), 
                    event.getRejectionReason() != null ? event.getRejectionReason() : "Nội dung chưa phù hợp");
        } else {
            return;
        }

        // Create In-App Notification
        notificationService.createNotification(
                blog.getUser(),
                title,
                content,
                NotificationType.MODERATION,
                "/blog/detail?id=" + blog.getId()
        );

        // Send Email
        try {
            mailService.sendMail(blog.getUser().getEmail(), title, content);
        } catch (Exception e) {
            log.error("Failed to send moderation email to {}: {}", blog.getUser().getEmail(), e.getMessage());
        }
    }
}
