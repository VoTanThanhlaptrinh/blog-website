package com.blog.be.admin.api;

import com.blog.be.admin.api.dto.AdminDashboardSummaryResponse;
import com.blog.be.admin.api.dto.DailyGrowthResponse;
import com.blog.be.admin.application.AdminService;
import com.blog.be.notification.api.ApiResponse;
import com.blog.be.content.api.dto.BlogResponse;
import com.blog.be.identity.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminService adminService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AdminDashboardSummaryResponse>> getDashboardSummary(
            @AuthenticationPrincipal User adminUser) {
        AdminDashboardSummaryResponse response = adminService.getDashboardSummary(adminUser);
        return ResponseEntity.ok(new ApiResponse<>(response, "Lấy tổng quan số liệu thống kê thành công", 200));
    }

    @GetMapping("/growth")
    public ResponseEntity<ApiResponse<List<DailyGrowthResponse>>> getGrowthStats(
            @RequestParam(defaultValue = "30") int days,
            @AuthenticationPrincipal User adminUser) {
        List<DailyGrowthResponse> response = adminService.getGrowthStats(days, adminUser);
        return ResponseEntity.ok(new ApiResponse<>(response, "Lấy dữ liệu tăng trưởng thành công", 200));
    }

    @GetMapping("/top-blogs")
    public ResponseEntity<ApiResponse<List<BlogResponse>>> getTopBlogs(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal User adminUser) {
        List<BlogResponse> response = adminService.getTopBlogs(limit, adminUser);
        return ResponseEntity.ok(new ApiResponse<>(response, "Lấy danh sách bài viết hàng đầu thành công", 200));
    }
}
