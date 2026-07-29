package com.blog.be.content.api;

import com.blog.be.content.api.dto.*;
import com.blog.be.content.application.BlogService;
import com.blog.be.content.domain.enums.BlogStatus;
import com.blog.be.content.domain.event.BlogImagesActivatedEvent;
import com.blog.be.identity.domain.entity.User;
import com.blog.be.notification.api.ApiResponse;
import com.blog.be.storage.api.dto.UpdateImagePrefixRequest;
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
