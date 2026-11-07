package com.blog.backend.admin.api;

import com.blog.backend.admin.application.AdminSystemSettingService;
import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.notification.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/settings")
@RequiredArgsConstructor
public class AdminSystemSettingsController {

    private final AdminSystemSettingService adminService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> getSettings(
            @AuthenticationPrincipal User adminUser) {
        Map<String, String> settings = adminService.getSystemSettings(adminUser);
        return ResponseEntity.ok(new ApiResponse<>(settings, "Lấy cấu hình hệ thống thành công", 200));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> updateSettings(
            @RequestBody Map<String, String> settings,
            @AuthenticationPrincipal User adminUser) {
        Map<String, String> updatedSettings = adminService.updateSystemSettings(settings, adminUser);
        return ResponseEntity.ok(new ApiResponse<>(updatedSettings, "Cập nhật cấu hình hệ thống thành công", 200));
    }
}
