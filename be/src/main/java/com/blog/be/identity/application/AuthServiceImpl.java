package com.blog.be.identity.application;

import com.blog.be.identity.api.dto.AccountLoginRequest;
import com.blog.be.identity.api.dto.AuthResponse;
import com.blog.be.identity.api.dto.ChangePasswordRequest;
import com.blog.be.identity.api.dto.RegisterRequest;
import com.blog.be.identity.domain.entity.User;
import com.blog.be.identity.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder bcryptEncoder;
    private final JwtService jwtService;
    @Value("${identity.expire-rt-day}")
    private Integer expireRtDay;
    @Value("${app.cookie.secure}")
    private boolean secureCookie;
    @Value("${app.cookie.same-site}")
    private String sameSite;
    @Override
    public AuthResponse login(AccountLoginRequest loginRequest, HttpServletResponse response) {
        Optional<User> user = userRepository.findUserByEmail(loginRequest.email());
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("email khong ton tai");
        }
        if(!bcryptEncoder.matches(loginRequest.password(), user.get().getPassword())) {
            throw new InvalidParameterException("mat khau khong khop");
        }

        User currentUser = user.get();
        String tokenValue = jwtService.generateRefreshToken(currentUser.getId());

        long maxAgeInSeconds = expireRtDay * 24L * 60 * 60;
        ResponseCookie springCookie = ResponseCookie.from("refreshToken", tokenValue)
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .maxAge(maxAgeInSeconds)
                .sameSite(sameSite) // Hoặc "Lax", "None" tùy nhu cầu
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, springCookie.toString());

        return AuthResponse.builder().accessToken(jwtService.generateAccessToken(currentUser.getId()
                , (List<GrantedAuthority>) currentUser.getAuthorities())).build();
    }

    @Override
    public void logout(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, "");
    }

    @Override
    public void register(RegisterRequest request) {

    }

    @Override
    public void changePassword(ChangePasswordRequest request) {

    }

    @Override
    public void forgotPassword(ChangePasswordRequest request) {

    }

    @Override
    public AuthResponse refreshToken(HttpServletRequest request) {
        return null;
    }
}
