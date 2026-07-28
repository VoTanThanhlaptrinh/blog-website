package com.blog.be.admin.api;

import com.blog.be.infrastructure.seeder.DataSeeder;
import com.blog.be.notification.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminSeedController {

    private final DataSeeder dataSeeder;

    @PostMapping("/seed-data")
    public ResponseEntity<ApiResponse<String>> triggerSeedData() {
        dataSeeder.seedData();
        return ResponseEntity.ok(new ApiResponse<>("OK", "Khởi tạo dữ liệu mẫu thành công!", 200));
    }
}
