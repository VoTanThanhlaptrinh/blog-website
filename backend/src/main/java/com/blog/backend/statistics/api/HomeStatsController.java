package com.blog.backend.statistics.api;

import com.blog.backend.notification.api.ApiResponse;
import com.blog.backend.statistics.application.HomeStatsService;
import com.blog.backend.statistics.dto.HomeStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class HomeStatsController {

    private final HomeStatsService homeStatsService;

    @GetMapping("/home")
    public ResponseEntity<ApiResponse<HomeStatsResponse>> getHomeStats() {
        HomeStatsResponse response = homeStatsService.getHomeStats();
        return ResponseEntity.ok(new ApiResponse<>(response, "Lấy thông tin thống kê trang chủ thành công", 200));
    }
}
