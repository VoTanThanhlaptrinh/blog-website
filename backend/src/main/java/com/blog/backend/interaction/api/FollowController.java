package com.blog.backend.interaction.api;

import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.interaction.api.dto.FollowStatusResponse;
import com.blog.backend.interaction.application.itf.FollowService;
import com.blog.backend.notification.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{userId}/follow")
    public ResponseEntity<ApiResponse<FollowStatusResponse>> toggleFollow(
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        FollowStatusResponse res = followService.toggleFollow(currentUser, userId);
        return ResponseEntity.ok(new ApiResponse<>(res, "Thao tác theo dõi thành công", 200));
    }

    @GetMapping("/{userId}/follow-status")
    public ResponseEntity<ApiResponse<FollowStatusResponse>> getFollowStatus(
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        FollowStatusResponse res = followService.getFollowStatus(currentUser, userId);
        return ResponseEntity.ok(new ApiResponse<>(res, "Lấy trạng thái theo dõi thành công", 200));
    }
}
