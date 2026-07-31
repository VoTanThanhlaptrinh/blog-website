package com.blog.backend.admin.api;

import com.blog.backend.admin.api.dto.AdminUserResponse;
import com.blog.backend.admin.api.dto.UpdateUserRoleRequest;
import com.blog.backend.admin.api.dto.UpdateUserStatusRequest;
import com.blog.backend.admin.application.AdminService;
import com.blog.backend.content.api.dto.PageResponse;
import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.identity.domain.enums.UserStatus;
import com.blog.backend.notification.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminUserResponse>>> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @AuthenticationPrincipal User adminUser) {

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<AdminUserResponse> response = adminService.getUsers(role, status, keyword, pageable, adminUser);
        return ResponseEntity.ok(new ApiResponse<>(response, "Lấy danh sách người dùng thành công", 200));
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody UpdateUserStatusRequest request,
            @AuthenticationPrincipal User adminUser) {
        AdminUserResponse response = adminService.updateUserStatus(userId, request, adminUser);
        return ResponseEntity.ok(new ApiResponse<>(response, "Cập nhật trạng thái người dùng thành công", 200));
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateUserRole(
            @PathVariable Long userId,
            @RequestBody UpdateUserRoleRequest request,
            @AuthenticationPrincipal User adminUser) {
        AdminUserResponse response = adminService.updateUserRole(userId, request, adminUser);
        return ResponseEntity.ok(new ApiResponse<>(response, "Cập nhật vai trò người dùng thành công", 200));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal User adminUser) {
        byte[] csvData = adminService.exportUsersCsv(role, status, keyword, adminUser);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users_export.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}
