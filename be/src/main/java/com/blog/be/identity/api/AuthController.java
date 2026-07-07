package com.blog.be.identity.api;

import com.blog.be.notification.api.ApiResponse;
import com.blog.be.identity.api.dto.*;
import com.blog.be.identity.application.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;
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

    @PostMapping("/login/social")
    public ResponseEntity<AuthResponse> socialLogin(@RequestBody SocialLoginRequest request) {
        // TODO: Implement social login logic (Google, Facebook)
        return ResponseEntity.ok(new AuthResponse());
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
    public ResponseEntity<UserProfileResponse> getProfile() {
        // TODO: Get current user profile based on authentication context
        return ResponseEntity.ok(new UserProfileResponse());
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(Principal principal, @Valid @RequestBody UpdateProfileRequest request
    , BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            return ResponseEntity.badRequest().body(new ApiResponse<>(null
                    , Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), 400));
        }
        Long userId = Long.valueOf(principal.getName());
        UserProfileResponse res = authService.updateProfile(userId, request);
        return ResponseEntity.ok(new  ApiResponse<>(res, "Cập nhật thành công", 200));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request
            , BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            return ResponseEntity.badRequest().body(new ApiResponse<>(null
                    , Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), 400));
        }
        return ResponseEntity.ok().build();
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
        response.addHeader(HttpHeaders.SET_COOKIE, "");
        return ResponseEntity.ok().build();
    }
    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/activeAccount")
    public ResponseEntity<ApiResponse<AuthResponse>> activeAccount(@RequestParam String token) {
        authService.activeAccount(token);
        return ResponseEntity.ok(new ApiResponse<>(null, "Kích hoạt thành công", 200));
    }
}
