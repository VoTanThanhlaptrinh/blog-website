package com.blog.backend.identity.application;

import com.blog.backend.identity.api.dto.AccountLoginRequest;
import com.blog.backend.identity.api.dto.AuthResponse;
import com.blog.backend.identity.api.dto.ChangePasswordRequest;
import com.blog.backend.identity.api.dto.RegisterRequest;
import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.identity.domain.event.UserRegistrationEvent;
import com.blog.backend.identity.domain.repository.UserRepository;
import com.blog.backend.identity.domain.exception.UsernameAlreadyExistsException;
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
import com.blog.backend.identity.domain.exception.InvalidTokenException;
import com.blog.backend.identity.domain.exception.UserNotFoundException;
import com.blog.backend.identity.api.dto.ForgotPasswordRequest;
import com.blog.backend.identity.api.dto.ResetPasswordRequest;
import com.blog.backend.identity.api.dto.UpdateProfileRequest;
import com.blog.backend.identity.api.dto.UserProfileResponse;
import com.blog.backend.identity.api.dto.VerifyOtpRequest;
import com.blog.backend.identity.domain.event.ForgotPasswordEvent;
import com.blog.backend.identity.domain.event.ProfileImageConfirmEvent;
import com.blog.backend.storage.domain.entity.Image;
import com.blog.backend.identity.domain.exception.AccountAlreadyActiveException;
import com.blog.backend.identity.domain.exception.ExpiredOtpException;
import com.blog.backend.identity.domain.exception.IncorrectPasswordException;
import com.blog.backend.identity.domain.exception.InvalidOtpException;
import com.blog.backend.identity.domain.exception.InvalidResetTokenException;
import com.blog.backend.identity.domain.exception.PasswordMismatchException;
import org.springframework.data.redis.core.RedisTemplate;

import java.security.Principal;
import java.util.concurrent.TimeUnit;
import java.util.UUID;
import java.util.Random;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final String FORGOT_PASSWORD_OTP_KEY_PREFIX = "forgot_password_otp:";
    private static final String FORGOT_PASSWORD_TOKEN_KEY_PREFIX = "forgot_password_token:";

    private final UserRepository userRepository;
    private final PasswordEncoder bcryptEncoder;
    private final JwtService jwtService;
    @Value("${identity.expire-rt-day}")
    private Integer expireRtDay;
    @Value("${app.cookie.secure}")
    private boolean secureCookie;
    @Value("${app.cookie.same-site}")
    private String sameSite;
    @Value("${app.domain}")
    private String domain;
    private final ApplicationEventPublisher publisher;
    private final RedisTemplate<String, String> redisTemplate;

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    @Override
    public AuthResponse login(AccountLoginRequest loginRequest, HttpServletResponse response) {
        Optional<User> user = userRepository.findUserByEmail(loginRequest.email());
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("email khong ton tai");
        }
        if (!bcryptEncoder.matches(loginRequest.password(), user.get().getPassword())) {
            throw new InvalidParameterException("mat khau khong khop");
        }

        User currentUser = user.get();
        // Generate a single access token using expireRtDay to serve as a session token
        String tokenValue = jwtService.generateAccessToken(currentUser.getId(),
                (List<GrantedAuthority>) currentUser.getAuthorities());

        long maxAgeInSeconds = expireRtDay * 24L * 60 * 60;
        ResponseCookie springCookie = ResponseCookie.from("token", tokenValue)
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .maxAge(maxAgeInSeconds)
                .sameSite(sameSite)
                .domain(domain)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, springCookie.toString());

        UserProfileResponse userProfile = UserProfileResponse.builder()
                .id(currentUser.getId())
                .email(currentUser.getEmail())
                .phone(currentUser.getPhone())
                .bio(currentUser.getBio())
                .birthDate(currentUser.getBirthDate() != null ? currentUser.getBirthDate().toString() : null)
                .avatarUrl(currentUser.getAvatar() != null ? currentUser.getAvatar().getUrl() : null)
                .roles(currentUser.getAuthorities() != null ? currentUser.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList() : java.util.List.of())
                .build();

        return AuthResponse.builder()
                .user(userProfile)
                .build();
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
        UserRegistrationEvent event = new UserRegistrationEvent(this, request.email(), token);
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
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setPhone(request.getPhone());
        user.setBio(request.getBio());
        user.setBirthDate(request.getBirthDate());

        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()) {
            if (user.getAvatar() == null) {
                user.setAvatar(new Image());
            }
            user.getAvatar().setUrl(request.getAvatarUrl());

            // Lưu và flush để sinh ID cho Image
            userRepository.saveAndFlush(user);

            ProfileImageConfirmEvent event = new ProfileImageConfirmEvent(this, user.getAvatar().getId(),
                    request.getAvatarUrl());
            publisher.publishEvent(event);
        } else {
            userRepository.save(user);
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .bio(user.getBio())
                .birthDate(user.getBirthDate() != null ? user.getBirthDate().toString() : null)
                .avatarUrl(user.getAvatar() != null ? user.getAvatar().getUrl() : null)
                .roles(user.getAuthorities() != null ? user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList() : java.util.List.of())
                .build();
    }

    /**
     * Quên mật khẩu: Tạo mã OTP ngẫu nhiên 6 chữ số, lưu vào Redis với TTL 2 phút,
     * và phát sự kiện gửi Email.
     */
    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Tài khoản không tồn tại!"));

        String otp = generateOtp();
        String key = FORGOT_PASSWORD_OTP_KEY_PREFIX + request.getEmail();

        // Lưu OTP vào Redis với thời hạn hết hạn 2 phút
        redisTemplate.opsForValue().set(key, otp, 2, TimeUnit.MINUTES);

        // Phát sự kiện gửi email chứa OTP cho người dùng
        publisher.publishEvent(new ForgotPasswordEvent(this, request.getEmail(), otp));
    }

    /**
     * Xác thực OTP: Kiểm tra OTP trong Redis. Nếu chính xác, cấp Reset Token (UUID)
     * lưu vào Redis trong 15 phút.
     */
    @Override
    public String verifyOtp(VerifyOtpRequest request) {
        String key = FORGOT_PASSWORD_OTP_KEY_PREFIX + request.getEmail();
        String savedOtp = redisTemplate.opsForValue().get(key);

        if (savedOtp == null) {
            throw new ExpiredOtpException("Mã OTP đã hết hạn hoặc không tồn tại!");
        }

        if (!savedOtp.equals(request.getOtp())) {
            throw new InvalidOtpException("Mã OTP không chính xác!");
        }

        User user = userRepository.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Tài khoản không tồn tại!"));

        // Sinh ngẫu nhiên UUID token đại diện cho phiên đặt lại mật khẩu
        String uuid = UUID.randomUUID().toString();
        String tokenKey = FORGOT_PASSWORD_TOKEN_KEY_PREFIX + uuid;

        // Lưu mapping UUID -> userId vào Redis với TTL 15 phút
        redisTemplate.opsForValue().set(tokenKey, String.valueOf(user.getId()), 15, TimeUnit.MINUTES);

        // Xóa mã OTP sau khi đã xác thực thành công
        redisTemplate.delete(key);

        return uuid;
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("Mật khẩu xác nhận không khớp!");
        }

        String tokenKey = FORGOT_PASSWORD_TOKEN_KEY_PREFIX + request.getToken();
        String userIdStr = redisTemplate.opsForValue().get(tokenKey);

        if (userIdStr == null) {
            throw new InvalidResetTokenException("Token không hợp lệ hoặc đã hết hạn!");
        }

        Long userId = Long.valueOf(userIdStr);
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("Tài khoản không tồn tại!"));

        user.setPassword(bcryptEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Delete token from Redis
        redisTemplate.delete(tokenKey);
    }

    @Override
    @Transactional
    public void activeAccount(String token) {
        // kiểm tra tính hợp lệ của token
        if (token == null || !jwtService.isTokenValid(token)) {
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
        // kích hoạt tài khoản
        user.active();
        userRepository.save(user);
    }

    @Override
    public UserProfileResponse profile(User currentUser) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new UserNotFoundException("Tài khoản không tồn tại!");
        }
        long userId = currentUser.getId();
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("Tài khoản không tồn tại!"));
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .bio(user.getBio())
                .birthDate(user.getBirthDate() != null ? user.getBirthDate().toString() : null)
                .avatarUrl(user.getAvatar() != null ? user.getAvatar().getUrl() : null)
                .roles(user.getAuthorities() != null ? user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList() : java.util.List.of())
                .build();
    }

    @Override
    public AuthResponse refreshToken(String token) {
        return new AuthResponse(); // Not used anymore
    }

    @Override
    @Transactional
    public void deactivateAccount(Long userId) {
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("Tài khoản không tồn tại!"));
        user.setEnabled(false);
        user.setStatus(com.blog.backend.identity.domain.enums.UserStatus.INACTIVE);
        userRepository.save(user);
    }
}
