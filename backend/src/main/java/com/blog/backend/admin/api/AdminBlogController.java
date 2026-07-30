package com.blog.backend.admin.api;

import com.blog.backend.admin.api.dto.RejectBlogRequest;
import com.blog.backend.admin.application.AdminService;
import com.blog.backend.notification.api.ApiResponse;
import com.blog.backend.content.api.dto.BlogResponse;
import com.blog.backend.content.api.dto.PageResponse;
import com.blog.backend.content.domain.enums.BlogStatus;
import com.blog.backend.identity.domain.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/blogs")
@RequiredArgsConstructor
public class AdminBlogController {

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BlogResponse>>> getBlogsForModeration(
            @RequestParam(required = false) BlogStatus status,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal User adminUser) {
        PageResponse<BlogResponse> result = adminService.getBlogsForModeration(status, keyword, pageable, adminUser);
        return ResponseEntity.ok(new ApiResponse<>(result, "Lấy danh sách bài viết duyệt thành công", 200));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<BlogResponse>> approveBlog(
            @PathVariable Long id,
            @AuthenticationPrincipal User adminUser) {
        BlogResponse response = adminService.approveBlog(id, adminUser);
        return ResponseEntity.ok(new ApiResponse<>(response, "Đã phê duyệt bài viết", 200));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<BlogResponse>> rejectBlog(
            @PathVariable Long id,
            @Valid @RequestBody RejectBlogRequest request,
            @AuthenticationPrincipal User adminUser) {
        BlogResponse response = adminService.rejectBlog(id, request, adminUser);
        return ResponseEntity.ok(new ApiResponse<>(response, "Đã từ chối xuất bản bài viết", 200));
    }
}
