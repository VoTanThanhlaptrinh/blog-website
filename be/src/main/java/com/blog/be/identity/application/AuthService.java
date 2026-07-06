package com.blog.be.identity.application;

import com.blog.be.identity.api.dto.AccountLoginRequest;
import com.blog.be.identity.api.dto.AuthResponse;
import com.blog.be.identity.api.dto.ChangePasswordRequest;
import com.blog.be.identity.api.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    
     AuthResponse login(AccountLoginRequest loginRequest, HttpServletResponse response);

   
     void logout(HttpServletResponse response);

    
     void register(RegisterRequest request) ;

    
     void changePassword(ChangePasswordRequest request); 

    
     void forgotPassword(ChangePasswordRequest request);

     AuthResponse refreshToken(HttpServletRequest request);
}
