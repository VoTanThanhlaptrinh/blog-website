package com.blog.backend.interaction.api;

import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.interaction.api.dto.CommentLikeResponse;
import com.blog.backend.interaction.api.dto.LikeResponse;
import com.blog.backend.interaction.api.dto.ToggleCommentLikeRequest;
import com.blog.backend.interaction.api.dto.ToggleLikeRequest;
import com.blog.backend.interaction.application.itf.LikeService;
import com.blog.backend.notification.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/toggle")
    public ResponseEntity<ApiResponse<LikeResponse>> toggleLike(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ToggleLikeRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null,
                    Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), 400));
        }

        LikeResponse res = likeService.toggleLike(currentUser, request);
        return ResponseEntity.ok(new ApiResponse<>(res, "Thao tác thích bài viết thành công", 200));
    }

    @PostMapping("/comment/toggle")
    public ResponseEntity<ApiResponse<CommentLikeResponse>> toggleCommentLike(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ToggleCommentLikeRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null,
                    Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), 400));
        }

        CommentLikeResponse res = likeService.toggleCommentLike(currentUser, request);
        return ResponseEntity.ok(new ApiResponse<>(res, "Thao tác thích bình luận thành công", 200));
    }
}
