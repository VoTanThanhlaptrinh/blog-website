package com.blog.backend.notification.application;

import com.blog.backend.content.api.dto.PageResponse;
import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.notification.api.dto.NotificationResponse;
import com.blog.backend.notification.domain.entity.Notification;
import com.blog.backend.notification.domain.enums.NotificationType;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    Notification createNotification(User recipient, String title, String content, NotificationType type, String relatedUrl);
    PageResponse<NotificationResponse> getUserNotifications(User currentUser, Pageable pageable);
    long getUnreadCount(User currentUser);
    void markAsRead(Long notificationId, User currentUser);
    void markAllAsRead(User currentUser);
}
