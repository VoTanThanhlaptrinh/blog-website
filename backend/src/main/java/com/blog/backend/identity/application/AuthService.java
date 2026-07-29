package com.blog.be.identity.application;

import com.blog.be.identity.api.dto.AccountLoginRequest;
import com.blog.be.identity.api.dto.AuthResponse;
import com.blog.be.identity.api.dto.ChangePasswordRequest;
import com.blog.be.identity.api.dto.ForgotPasswordRequest;
import com.blog.be.identity.api.dto.RegisterRequest;
import com.blog.be.identity.api.dto.ResetPasswordRequest;
import com.blog.be.identity.api.dto.VerifyOtpRequest;
import com.blog.be.identity.api.dto.UpdateProfileRequest;
import com.blog.be.identity.api.dto.UserProfileResponse;
import com.blog.be.identity.domain.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.security.Principal;

public interface AuthService {

     AuthResponse login(AccountLoginRequest loginRequest, HttpServletResponse response);

     void register(RegisterRequest request);

     void changePassword(Long userId, ChangePasswordRequest request);

     UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request);

     void forgotPassword(ForgotPasswordRequest request);

     String verifyOtp(VerifyOtpRequest request);

     void resetPassword(ResetPasswordRequest request);

     // AuthResponse refreshToken(HttpServletRequest request);

     void activeAccount(String token);

     UserProfileResponse profile(Principal principal);

     AuthResponse refreshToken(String token);
}
