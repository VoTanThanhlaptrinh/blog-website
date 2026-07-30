package com.blog.backend.admin.api;

import com.blog.backend.admin.api.dto.ReportResponse;
import com.blog.backend.admin.api.dto.ResolveReportRequest;
import com.blog.backend.admin.application.AdminService;
import com.blog.backend.admin.domain.enums.ReportStatus;
import com.blog.backend.admin.domain.enums.ReportTargetType;
import com.blog.backend.notification.api.ApiResponse;
import com.blog.backend.content.api.dto.PageResponse;
import com.blog.backend.identity.domain.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ReportResponse>>> getReports(
            @RequestParam(required = false) ReportTargetType targetType,
            @RequestParam(required = false) ReportStatus status,
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal User adminUser) {
        PageResponse<ReportResponse> result = adminService.getReports(targetType, status, pageable, adminUser);
        return ResponseEntity.ok(new ApiResponse<>(result, "Lấy danh sách báo cáo thành công", 200));
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<ReportResponse>> resolveReport(
            @PathVariable Long id,
            @Valid @RequestBody ResolveReportRequest request,
            @AuthenticationPrincipal User adminUser) {
        ReportResponse response = adminService.resolveReport(id, request, adminUser);
        return ResponseEntity.ok(new ApiResponse<>(response, "Đã cập nhật trạng thái xử lý báo cáo", 200));
    }
}
