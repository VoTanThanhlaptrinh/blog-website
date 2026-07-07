package com.blog.be.identity.application;

import com.blog.be.identity.api.dto.AccountLoginRequest;
import com.blog.be.identity.api.dto.AuthResponse;
import com.blog.be.identity.api.dto.ChangePasswordRequest;
import com.blog.be.identity.api.dto.RegisterRequest;
import com.blog.be.identity.domain.entity.User;
import com.blog.be.identity.domain.event.UserRegistrationEvent;
import com.blog.be.identity.domain.repository.UserRepository;
import com.blog.be.identity.domain.exception.UsernameAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import com.blog.be.identity.domain.exception.InvalidTokenException;
import com.blog.be.identity.domain.exception.UserNotFoundException;
import com.blog.be.identity.api.dto.UpdateProfileRequest;
import com.blog.be.identity.api.dto.UserProfileResponse;
import com.blog.be.identity.domain.exception.AccountAlreadyActiveException;
import com.blog.be.identity.domain.exception.IncorrectPasswordException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.InvalidParameterException;
import java.time.LocalDate;
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
    private final ApplicationEventPublisher publisher;
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
    public void register(RegisterRequest request) {
        Optional<User> optionalUser = userRepository.findUserByEmail(request.email());
        if (optionalUser.isPresent()) {
            throw new UsernameAlreadyExistsException("Email đã được sử dụng!");
        }
        User user = User.builder().email(request.email())
                .password(bcryptEncoder.encode(request.password())).build();
        userRepository.saveAndFlush(user);
        String token = jwtService.generateActiveToken(user.getId());
        UserRegistrationEvent event = new UserRegistrationEvent(request.email(), token);
        publisher.publishEvent(event);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        if (!bcryptEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IncorrectPasswordException("Mật khẩu cũ không chính xác");
        }
        
        user.setPassword(bcryptEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    
    @Override
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        user.setPhone(request.getPhone());
        user.setBio(request.getBio());
        user.setBirthDate(request.getBirthDate());
        
        userRepository.save(user);
        
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .bio(user.getBio())
                .birthDate(user.getBirthDate())
                .build();
    }

    @Override
    public void forgotPassword(ChangePasswordRequest request) {

    }

    @Override
    public AuthResponse refreshToken(HttpServletRequest request) {
        return null;
    }

    @Override
    @Transactional
    public void activeAccount(String token) {
        // kiểm tra tính hợp lệ của token
        if(token == null || !jwtService.isTokenValid(token)){
            throw new InvalidTokenException("Token kích hoạt không hợp lệ hoặc đã hết hạn!");
        }
        // lấy ra userId từ token
        String subjectClaim = jwtService.getClaim(token, "sub", String.class);
        Long userId = Long.valueOf(subjectClaim);
        // lấy ra user từ id
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("Tài khoản không tồn tại!"));
        // kiểm tra user đã kích hoạt chưa tránh việc gọi thêm một sql
        if (user.isEnabled()) {
            throw new AccountAlreadyActiveException("Tài khoản này đã được kích hoạt rồi!");
        }
        //  kích hoạt tài khoản
        user.active();
        userRepository.save(user);
    }
}
