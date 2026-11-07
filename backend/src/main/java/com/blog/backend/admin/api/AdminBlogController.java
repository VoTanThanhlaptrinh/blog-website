package com.blog.backend.admin.api;

import com.blog.backend.admin.api.dto.RejectBlogRequest;
import com.blog.backend.admin.application.AdminBlogService;
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

import com.blog.backend.admin.domain.event.AdminGetBlogEvent;
import org.springframework.context.ApplicationEventPublisher;

@RestController
@RequestMapping("/api/v1/admin/blogs")
@RequiredArgsConstructor
public class AdminBlogController {

    private final AdminBlogService adminBlogService;
    private final ApplicationEventPublisher eventPublisher;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BlogResponse>> getBlogById(
            @PathVariable Long id,
            @AuthenticationPrincipal User adminUser) {
        AdminGetBlogEvent event = new AdminGetBlogEvent(this, id, adminUser);
        eventPublisher.publishEvent(event);
        return ResponseEntity.ok(new ApiResponse<>(event.getResult(), "Lấy chi tiết bài viết thành công", 200));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BlogResponse>>> getBlogsForModeration(
            @RequestParam(required = false) BlogStatus status,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal User adminUser) {
        PageResponse<BlogResponse> result = adminBlogService.getBlogsForModeration(status, keyword, pageable, adminUser);
        return ResponseEntity.ok(new ApiResponse<>(result, "Lấy danh sách bài viết duyệt thành công", 200));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<BlogResponse>> approveBlog(
            @PathVariable Long id,
            @AuthenticationPrincipal User adminUser) {
        BlogResponse response = adminBlogService.approveBlog(id, adminUser);
        return ResponseEntity.ok(new ApiResponse<>(response, "Đã phê duyệt bài viết", 200));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<BlogResponse>> rejectBlog(
            @PathVariable Long id,
            @Valid @RequestBody RejectBlogRequest request,
            @AuthenticationPrincipal User adminUser) {
        BlogResponse response = adminBlogService.rejectBlog(id, request, adminUser);
        return ResponseEntity.ok(new ApiResponse<>(response, "Đã từ chối xuất bản bài viết", 200));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportBlogs(
            @RequestParam(required = false) BlogStatus status,
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal User adminUser) {
        byte[] csvData = adminBlogService.exportBlogsCsv(status, keyword, adminUser);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=blogs_export.csv")
                .contentType(org.springframework.http.MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}
