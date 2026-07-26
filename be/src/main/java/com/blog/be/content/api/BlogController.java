package com.blog.be.content.api;

import com.blog.be.content.api.dto.*;
import com.blog.be.content.application.BlogService;
import com.blog.be.content.domain.enums.BlogStatus;
import com.blog.be.notification.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/blogs")
@RequiredArgsConstructor
public class BlogController {
    private final BlogService blogService;

    @PostMapping
    public ResponseEntity<ApiResponse<BlogResponse>> createBlog(@AuthenticationPrincipal Principal principal,
                                                                @Valid @RequestBody CreateBlogRequest request,
                                                                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null,
                    Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), 400));
        }
        BlogResponse res = blogService.createBlog(principal, request);
        return ResponseEntity.ok(new ApiResponse<>(res, "Tạo bài viết thành công", 200));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BlogResponse>> getBlogById(@PathVariable Long id,
                                                                 @AuthenticationPrincipal Principal principal) {
        BlogResponse res = blogService.getBlogById(id, principal);
        return ResponseEntity.ok(new ApiResponse<>(res, "Lấy chi tiết bài viết thành công", 200));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BlogResponse>>> getBlogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BlogStatus status,
            @RequestParam(required = false) Long userId,
            @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal Principal principal) {

        PageResponse<BlogResponse> res = blogService.getBlogs(keyword, status, userId, pageable, principal);
        return ResponseEntity.ok(new ApiResponse<>(res, "Lấy danh sách bài viết thành công", 200));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BlogResponse>> updateBlog(@PathVariable Long id,
                                                                @AuthenticationPrincipal Principal principal,
                                                                @Valid @RequestBody UpdateBlogRequest request,
                                                                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null,
                    Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), 400));
        }
        BlogResponse res = blogService.updateBlog(id, principal, request);
        return ResponseEntity.ok(new ApiResponse<>(res, "Cập nhật bài viết thành công", 200));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBlog(@PathVariable Long id,
                                                        @AuthenticationPrincipal Principal principal) {
        blogService.deleteBlog(id, principal);
        return ResponseEntity.ok(new ApiResponse<>(null, "Xóa bài viết thành công", 200));
    }
}
