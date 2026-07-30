package com.blog.backend.notification.api;

import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.notification.api.dto.NotificationSettingResponse;
import com.blog.backend.notification.api.dto.UpdateNotificationSettingRequest;
import com.blog.backend.notification.application.NotificationSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settings/notifications")
@RequiredArgsConstructor
public class NotificationSettingController {

    private final NotificationSettingService notificationSettingService;

    @GetMapping
    public ResponseEntity<ApiResponse<NotificationSettingResponse>> getSettings(
            @AuthenticationPrincipal User currentUser) {
        NotificationSettingResponse result = notificationSettingService.getNotificationSetting(currentUser);
        return ResponseEntity.ok(new ApiResponse<>(result, "Lấy cài đặt thông báo thành công", 200));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<NotificationSettingResponse>> updateSettings(
            @AuthenticationPrincipal User currentUser,
            @RequestBody UpdateNotificationSettingRequest request) {
        NotificationSettingResponse result = notificationSettingService.updateNotificationSetting(currentUser, request);
        return ResponseEntity.ok(new ApiResponse<>(result, "Cập nhật cài đặt thông báo thành công", 200));
    }
}
