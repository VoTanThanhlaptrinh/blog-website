package com.blog.backend.identity.api;

import com.blog.backend.notification.api.ApiResponse;
import com.blog.backend.identity.api.dto.*;
import com.blog.backend.identity.application.AuthService;
import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.identity.domain.event.ProfileImageUploadEvent;
import com.blog.backend.storage.api.dto.UploadUrlRequest;
import com.blog.backend.storage.api.dto.UploadPostResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final ApplicationEventPublisher applicationEventPublisher;
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AccountLoginRequest request
            , BindingResult bindingResult, HttpServletResponse response) {
        if(bindingResult.hasErrors()){
            return ResponseEntity.badRequest().body(new ApiResponse<>(null
                    , Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), 400));
        }
        AuthResponse res = authService.login(request,response);
        return ResponseEntity.ok(new ApiResponse<>(res, "Dang nhap thanh cong", 200));
    }

    @GetMapping("/login/social")
    public ResponseEntity<ApiResponse<Map<String, String>>> getSocialLoginUrls() {
        Map<String, String> urls = new HashMap<>();
        urls.put("google", "/oauth2/authorization/google");
        urls.put("facebook", "/oauth2/authorization/facebook");
        return ResponseEntity.ok(new ApiResponse<>(urls, "Danh sách URL đăng nhập mạng xã hội", 200));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request
            , BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            return ResponseEntity.badRequest().body(new ApiResponse<>(null
                    , Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), 400));
        }
        authService.register(request);
        return ResponseEntity.ok(new ApiResponse<>(null, "Dang ky thanh cong", 200));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(new ApiResponse<>(authService.profile(currentUser)
                , "Xem profile thanh cong", 200));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(@AuthenticationPrincipal User currentUser, @Valid @RequestBody UpdateProfileRequest request
    , BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            return ResponseEntity.badRequest().body(new ApiResponse<>(null
                    , Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), 400));
        }
        Long userId = currentUser.getId();
        UserProfileResponse res = authService.updateProfile(userId, request);
        return ResponseEntity.ok(new  ApiResponse<>(res, "Cập nhật thành công", 200));
    }

    @PostMapping("/profile/avatar/upload-url")
    public ResponseEntity<ApiResponse<UploadPostResponse>> getAvatarUploadUrl(@AuthenticationPrincipal User currentUser, @Valid @RequestBody UploadUrlRequest request
            , BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            return ResponseEntity.badRequest().body(new ApiResponse<>(null
                    , Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), 400));
        }
        
        ProfileImageUploadEvent event = new ProfileImageUploadEvent(this, request);
        applicationEventPublisher.publishEvent(event);
        
        return ResponseEntity.ok(new ApiResponse<>(event.getResponse(), "Lấy upload url thành công", 200));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request
            , BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            return ResponseEntity.badRequest().body(new ApiResponse<>(null
                    , Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), 400));
        }
        authService.forgotPassword(request);
        return ResponseEntity.ok(new ApiResponse<>(null, "Đã gửi mã OTP đến email của bạn", 200));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request
            , BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            return ResponseEntity.badRequest().body(new ApiResponse<>(null
                    , Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), 400));
        }
        String token = authService.verifyOtp(request);
        return ResponseEntity.ok(new ApiResponse<>(token, "Xác thực OTP thành công", 200));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request
            , BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            return ResponseEntity.badRequest().body(new ApiResponse<>(null
                    , Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), 400));
        }
        authService.resetPassword(request);
        return ResponseEntity.ok(new ApiResponse<>(null, "Đặt lại mật khẩu thành công", 200));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(Principal principal, @Valid @RequestBody ChangePasswordRequest request
            , BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            return ResponseEntity.badRequest().body(new ApiResponse<>(null
                    , Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), 400));
        }
        Long userId = Long.valueOf(principal.getName());
        authService.changePassword(userId, request);
        return ResponseEntity.ok(new ApiResponse<>(null, "Đổi mật khẩu thành công", 200));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie springCookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, springCookie.toString());
        return ResponseEntity.ok().build();
    }


    @GetMapping("/activeAccount")
    public ResponseEntity<ApiResponse<Void>> activeAccount(@RequestParam String token) {
        authService.activeAccount(token);
        return ResponseEntity.ok(new ApiResponse<>(null, "Kích hoạt thành công", 200));
    }

    @PutMapping("/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateAccount(Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        authService.deactivateAccount(userId);
        return ResponseEntity.ok(new ApiResponse<>(null, "Vô hiệu hóa tài khoản thành công", 200));
    }
}
