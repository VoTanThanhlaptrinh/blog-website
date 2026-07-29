package com.blog.backend.admin.api;

import com.blog.backend.admin.api.dto.CreateReportRequest;
import com.blog.backend.admin.api.dto.ReportResponse;
import com.blog.backend.admin.application.AdminService;
import com.blog.backend.notification.api.ApiResponse;
import com.blog.backend.identity.domain.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final AdminService adminService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReportResponse>> createReport(
            @Valid @RequestBody CreateReportRequest request,
            @AuthenticationPrincipal User reporter) {
        ReportResponse response = adminService.createReport(reporter, request);
        return ResponseEntity.ok(new ApiResponse<>(response, "Gửi báo cáo thành công. Cảm ơn bạn đã phản hồi!", 200));
    }
}
