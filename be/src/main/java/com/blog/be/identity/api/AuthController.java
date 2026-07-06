package com.blog.be.identity.api;

import com.blog.be.common.api.ApiResponse;
import com.blog.be.identity.api.dto.*;
import com.blog.be.identity.application.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Validated @RequestBody AccountLoginRequest request
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
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        // TODO: Implement registration logic
        return ResponseEntity.ok(new AuthResponse());
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile() {
        // TODO: Get current user profile based on authentication context
        return ResponseEntity.ok(new UserProfileResponse());
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(@RequestBody UpdateProfileRequest request) {
        // TODO: Update user profile logic
        return ResponseEntity.ok(new UserProfileResponse());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        // TODO: Implement forgot password logic (send email)
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        // TODO: Implement change password logic
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // TODO: Implement logout logic (e.g., invalidate token)
        return ResponseEntity.ok().build();
    }
    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {

        return ResponseEntity.ok().build();
    }
}
