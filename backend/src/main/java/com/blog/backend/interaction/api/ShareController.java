package com.blog.backend.interaction.api;

import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.interaction.api.dto.CreateShareRequest;
import com.blog.backend.interaction.api.dto.ShareResponse;
import com.blog.backend.interaction.application.ShareService;
import com.blog.backend.notification.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/shares")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @PostMapping
    public ResponseEntity<ApiResponse<ShareResponse>> createShare(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateShareRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null,
                    Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), 400));
        }

        ShareResponse res = shareService.createShare(currentUser, request);
        return ResponseEntity.ok(new ApiResponse<>(res, "Ghi nhận chia sẻ bài viết thành công", 200));
    }
}
