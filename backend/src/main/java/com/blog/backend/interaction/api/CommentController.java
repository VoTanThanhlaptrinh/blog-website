package com.blog.backend.interaction.api;

import com.blog.backend.content.api.dto.PageResponse;
import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.interaction.api.dto.CommentResponse;
import com.blog.backend.interaction.api.dto.CreateCommentRequest;
import com.blog.backend.interaction.api.dto.UpdateCommentRequest;
import com.blog.backend.interaction.application.itf.CommentService;
import com.blog.backend.notification.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateCommentRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null,
                    Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), 400));
        }

        CommentResponse res = commentService.createComment(currentUser, request);
        return ResponseEntity.ok(new ApiResponse<>(res, "Tạo bình luận thành công", 200));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateCommentRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null,
                    Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), 400));
        }

        CommentResponse res = commentService.updateComment(id, currentUser, request);
        return ResponseEntity.ok(new ApiResponse<>(res, "Cập nhật bình luận thành công", 200));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        commentService.deleteComment(id, currentUser);
        return ResponseEntity.ok(new ApiResponse<>(null, "Xóa bình luận thành công", 200));
    }

    @GetMapping("/blog/{blogId}")
    public ResponseEntity<ApiResponse<PageResponse<CommentResponse>>> getCommentsByBlogId(
            @PathVariable Long blogId,
            @PageableDefault(sort = "createdDate", direction = Sort.Direction.ASC) Pageable pageable,
            @AuthenticationPrincipal User currentUser) {

        PageResponse<CommentResponse> res = commentService.getCommentsByBlogId(blogId, pageable, currentUser);
        return ResponseEntity.ok(new ApiResponse<>(res, "Lấy danh sách bình luận thành công", 200));
    }
}
