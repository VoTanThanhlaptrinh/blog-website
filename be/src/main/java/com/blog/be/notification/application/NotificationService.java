package com.blog.be.notification.application;

import com.blog.be.content.api.dto.PageResponse;
import com.blog.be.identity.domain.entity.User;
import com.blog.be.notification.api.dto.NotificationResponse;
import com.blog.be.notification.domain.entity.Notification;
import com.blog.be.notification.domain.enums.NotificationType;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    Notification createNotification(User recipient, String title, String content, NotificationType type, String relatedUrl);
    PageResponse<NotificationResponse> getUserNotifications(User currentUser, Pageable pageable);
    long getUnreadCount(User currentUser);
    void markAsRead(Long notificationId, User currentUser);
    void markAllAsRead(User currentUser);
}
