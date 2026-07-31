package com.blog.backend.content.api;

import com.blog.backend.content.api.dto.*;
import com.blog.backend.content.application.BlogService;
import com.blog.backend.content.domain.enums.BlogStatus;
import com.blog.backend.content.domain.event.BlogImagesActivatedEvent;
import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.notification.api.ApiResponse;
import com.blog.backend.storage.api.dto.UpdateImagePrefixRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.List;

@RestController
@RequestMapping("/api/v1/blogs")
@RequiredArgsConstructor
public class BlogController {
    private final BlogService blogService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @PostMapping
    public ResponseEntity<ApiResponse<BlogResponse>> createBlog(@AuthenticationPrincipal User currentUser,
                                                                @Valid @RequestBody CreateBlogRequest request,
                                                                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null,
                    Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), 400));
        }
        BlogResponse res = blogService.createBlog(currentUser, request);
        return ResponseEntity.ok(new ApiResponse<>(res, "Tạo bài viết thành công", 200));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BlogResponse>> getBlogById(@PathVariable Long id,
                                                                 @AuthenticationPrincipal User currentUser) {
        BlogResponse res = blogService.getBlogById(id, currentUser);
        return ResponseEntity.ok(new ApiResponse<>(res, "Lấy chi tiết bài viết thành công", 200));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BlogResponse>>> getBlogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BlogStatus status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User currentUser) {

        PageResponse<BlogResponse> res = blogService.getBlogs(keyword, status, userId, categoryId, pageable, currentUser);
        return ResponseEntity.ok(new ApiResponse<>(res, "Lấy danh sách bài viết thành công", 200));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<BlogCursorResponse>> searchBlogsCursor(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal User currentUser) {

        BlogCursorResponse res = blogService.searchBlogsCursor(keyword, categories, lastId, limit, currentUser);
        return ResponseEntity.ok(new ApiResponse<>(res, "Tìm kiếm bài viết thành công", 200));
    }

    @GetMapping("/me/cursor")
    public ResponseEntity<ApiResponse<BlogCursorResponse>> getMyBlogsCursor(
            @RequestParam(required = false) BlogStatus status,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal User currentUser) {

        BlogCursorResponse res = blogService.getMyBlogsCursor(status, lastId, limit, currentUser);
        return ResponseEntity.ok(new ApiResponse<>(res, "Lấy danh sách bài viết cá nhân thành công", 200));
    }

    @GetMapping("/me/thumbnails")
    public ResponseEntity<ApiResponse<List<String>>> getMyUsedThumbnails(
            @AuthenticationPrincipal User currentUser) {
        List<String> thumbnails = blogService.getMyUsedThumbnails(currentUser);
        return ResponseEntity.ok(new ApiResponse<>(thumbnails, "Lấy danh sách ảnh bìa cá nhân thành công", 200));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BlogResponse>> updateBlog(@PathVariable Long id,
                                                                @AuthenticationPrincipal User currentUser,
                                                                @Valid @RequestBody UpdateBlogRequest request,
                                                                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null,
                    Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), 400));
        }
        BlogResponse res = blogService.updateBlog(id, currentUser, request);
        return ResponseEntity.ok(new ApiResponse<>(res, "Cập nhật bài viết thành công", 200));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBlog(@PathVariable Long id,
                                                        @AuthenticationPrincipal User currentUser) {
        blogService.deleteBlog(id, currentUser);
        return ResponseEntity.ok(new ApiResponse<>(null, "Xóa bài viết thành công", 200));
    }

    @PostMapping("/images/activate")
    public ResponseEntity<ApiResponse<Void>> activateBlogImages(
            @RequestBody @Valid UpdateImagePrefixRequest request,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null,
                    Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), 400));
        }

        try {
            applicationEventPublisher.publishEvent(
                    new BlogImagesActivatedEvent(this, request.getImageUrls(), "temp/", "blog/")
            );
            return ResponseEntity.ok(new ApiResponse<>(null, "Yêu cầu kích hoạt hình ảnh đã được gửi", 200));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, e.getMessage(), 400));
        }
    }
}
