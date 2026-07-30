package com.blog.backend.notification.application;

import com.blog.backend.content.api.dto.PageResponse;
import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.notification.api.dto.NotificationResponse;
import com.blog.backend.notification.domain.entity.Notification;
import com.blog.backend.notification.domain.enums.NotificationType;
import com.blog.backend.notification.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public Notification createNotification(User recipient, String title, String content, NotificationType type, String relatedUrl) {
        if (recipient == null) return null;
        Notification notification = Notification.builder()
                .recipient(recipient)
                .title(title)
                .content(content)
                .type(type)
                .isRead(false)
                .relatedUrl(relatedUrl)
                .build();
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getUserNotifications(User currentUser, Pageable pageable) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new IllegalArgumentException("Vui lòng đăng nhập để xem thông báo");
        }

        Page<Notification> pageResult = notificationRepository.findByRecipientIdOrderByCreatedDateDesc(currentUser.getId(), pageable);
        List<NotificationResponse> content = pageResult.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<NotificationResponse>builder()
                .content(content)
                .pageNumber(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(User currentUser) {
        if (currentUser == null || currentUser.getId() == null) return 0;
        return notificationRepository.countByRecipientIdAndIsReadFalse(currentUser.getId());
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, User currentUser) {
        if (currentUser == null || currentUser.getId() == null) return;
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông báo"));
        if (notification.getRecipient().getId().equals(currentUser.getId())) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(User currentUser) {
        if (currentUser == null || currentUser.getId() == null) return;
        notificationRepository.markAllAsReadByRecipientId(currentUser.getId());
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .type(notification.getType())
                .isRead(notification.isRead())
                .relatedUrl(notification.getRelatedUrl())
                .createdDate(notification.getCreatedDate())
                .build();
    }
}
