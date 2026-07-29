package com.blog.be.notification.api;

import com.blog.be.notification.api.ApiResponse;
import com.blog.be.content.api.dto.PageResponse;
import com.blog.be.identity.domain.entity.User;
import com.blog.be.notification.api.dto.NotificationResponse;
import com.blog.be.notification.application.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getUserNotifications(
            @AuthenticationPrincipal User currentUser,
            @PageableDefault(size = 10) Pageable pageable) {
        PageResponse<NotificationResponse> result = notificationService.getUserNotifications(currentUser, pageable);
        return ResponseEntity.ok(new ApiResponse<>(result, "Lấy danh sách thông báo thành công", 200));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal User currentUser) {
        long count = notificationService.getUnreadCount(currentUser);
        return ResponseEntity.ok(new ApiResponse<>(count, "Lấy số lượng thông báo chưa đọc thành công", 200));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        notificationService.markAsRead(id, currentUser);
        return ResponseEntity.ok(new ApiResponse<>(null, "Đã đánh dấu thông báo là đã đọc", 200));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal User currentUser) {
        notificationService.markAllAsRead(currentUser);
        return ResponseEntity.ok(new ApiResponse<>(null, "Đã đánh dấu tất cả thông báo là đã đọc", 200));
    }
}
