package com.blog.backend.interaction.api;

import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.interaction.api.dto.RecordViewRequest;
import com.blog.backend.interaction.api.dto.ViewResponse;
import com.blog.backend.interaction.application.itf.ViewService;
import com.blog.backend.notification.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/views")
@RequiredArgsConstructor
public class ViewController {

    private final ViewService viewService;

    @PostMapping("/record")
    public ResponseEntity<ApiResponse<ViewResponse>> recordView(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody RecordViewRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpServletRequest) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null,
                    Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), 400));
        }

        String ipAddress = getClientIp(httpServletRequest);
        ViewResponse res = viewService.recordView(currentUser, ipAddress, request);
        return ResponseEntity.ok(new ApiResponse<>(res, "Ghi nhận lượt xem thành công", 200));
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
