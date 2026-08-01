package com.blog.backend.identity.application;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blog.backend.identity.api.dto.AccountLoginRequest;
import com.blog.backend.identity.api.dto.AuthResponse;
import com.blog.backend.identity.api.dto.ChangePasswordRequest;
import com.blog.backend.identity.api.dto.ForgotPasswordRequest;
import com.blog.backend.identity.api.dto.RegisterRequest;
import com.blog.backend.identity.api.dto.ResetPasswordRequest;
import com.blog.backend.identity.api.dto.UpdateProfileRequest;
import com.blog.backend.identity.api.dto.UserProfileResponse;
import com.blog.backend.identity.api.dto.VerifyOtpRequest;
import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.identity.domain.enums.UserStatus;
import com.blog.backend.identity.domain.event.ForgotPasswordEvent;
import com.blog.backend.identity.domain.event.ProfileImageConfirmEvent;
import com.blog.backend.identity.domain.event.UserRegistrationEvent;
import com.blog.backend.identity.domain.exception.AccountAlreadyActiveException;
import com.blog.backend.identity.domain.exception.ExpiredOtpException;
import com.blog.backend.identity.domain.exception.IncorrectPasswordException;
import com.blog.backend.identity.domain.exception.InvalidOtpException;
import com.blog.backend.identity.domain.exception.InvalidResetTokenException;
import com.blog.backend.identity.domain.exception.InvalidTokenException;
import com.blog.backend.identity.domain.exception.PasswordMismatchException;
import com.blog.backend.identity.domain.exception.UserNotFoundException;
import com.blog.backend.identity.domain.exception.UsernameAlreadyExistsException;
import com.blog.backend.identity.domain.repository.UserRepository;
import com.blog.backend.storage.domain.entity.Image;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

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
        // Tìm user theo email
        Optional<User> user = userRepository.findUserByEmail(loginRequest.email());
        // kiểm tra user tồn tại
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("email khong ton tai");
        }
        // Kiểm tra mật khẩu có khớp hay ko
        if (!bcryptEncoder.matches(loginRequest.password(), user.get().getPassword())) {
            throw new InvalidParameterException("mat khau khong khop");
        }

        User currentUser = user.get();
        // Generate a single access token using expireRtDay to serve as a session token
        String tokenValue = generateToken(currentUser);

        // tạo ra cookie với tokenValue và các thuộc tính bảo mật
        ResponseCookie springCookie = generateCookie(tokenValue);

        response.addHeader(HttpHeaders.SET_COOKIE, springCookie.toString());

        // Map user to UserProfileResponse
        UserProfileResponse userProfile = mapToResponse(currentUser);

        return AuthResponse.builder()
                .user(userProfile)
                .build();
    }

    @Override
    public void register(RegisterRequest request) {
        // Tìm user theo email
        Optional<User> optionalUser = userRepository.findUserByEmail(request.email());

        // Kiểm tra xem email đã được dùng chưa
        if (optionalUser.isPresent()) {
            throw new UsernameAlreadyExistsException("Email đã được sử dụng!");
        }
        // tạo user với mật khẩu
        User user = User.builder().email(request.email())
                .password(bcryptEncoder.encode(request.password())).build();
        // lưu user
        userRepository.saveAndFlush(user);
        // khởi tạo otp cho user
        String token = jwtService.generateActiveToken(user.getId());
        // Tạo event để module notification gửi mail về cho user
        UserRegistrationEvent event = new UserRegistrationEvent(this, request.email(), token);
        publisher.publishEvent(event);

    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        // Tìm user theo id
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        // Kiểm tra mật khẩu cũ có khớp hay không
        if (!bcryptEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IncorrectPasswordException("Mật khẩu cũ không chính xác");
        }
        // Cập nhật mật khẩu mới
        user.setPassword(bcryptEncoder.encode(request.getNewPassword()));
        // Lưu user với mật khẩu mới
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        // 1. Fail Fast: Lấy user hoặc quăng lỗi ngay
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // 2. Cập nhật thông tin cơ bản
        updateBasicInfo(user, request);

        // 3. Xử lý logic cập nhật avatar (nếu có)
        handleAvatarUpdate(user, request.getAvatarUrl());

        // 4. Trả về kết quả
        return mapToResponse(user);

    }

    /**
     * Quên mật khẩu: Tạo mã OTP ngẫu nhiên 6 chữ số, lưu vào Redis với TTL 2 phút,
     * và phát sự kiện gửi Email.
     */
    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        // Tìm user theo email
        User user = userRepository.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Tài khoản không tồn tại!"));
        // Sinh mã OTP ngẫu nhiên 6 chữ số
        String otp = generateOtp();
        // Tạo key để lưu OTP vào Redis
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
        // Tạo key để lấy OTP từ Redis
        String key = FORGOT_PASSWORD_OTP_KEY_PREFIX + request.getEmail();
        // Lấy OTP đã lưu từ Redis
        String savedOtp = redisTemplate.opsForValue().get(key);

        // Kiểm tra OTP có tồn tại và hợp lệ
        if (savedOtp == null) {
            throw new ExpiredOtpException("Mã OTP đã hết hạn hoặc không tồn tại!");
        }
        // Kiểm tra OTP có khớp với OTP người dùng nhập vào
        if (!savedOtp.equals(request.getOtp())) {
            throw new InvalidOtpException("Mã OTP không chính xác!");
        }
        // Tìm user theo email
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

    /**
     * Đặt lại mật khẩu: Kiểm tra token trong Redis. Nếu hợp lệ, cập nhật mật khẩu
     * mới
     * cho user và xóa token khỏi Redis.
     */
    @Override
    public void resetPassword(ResetPasswordRequest request) {

        // Kiểm tra xem mật khẩu mới và mật khẩu xác nhận có khớp nhau không
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("Mật khẩu xác nhận không khớp!");
        }
        // Tạo key để lấy userId từ Redis dựa trên token
        String tokenKey = FORGOT_PASSWORD_TOKEN_KEY_PREFIX + request.getToken();
        // Lấy userId từ Redis
        String userIdStr = redisTemplate.opsForValue().get(tokenKey);

        // Kiểm tra xem userId có tồn tại không, nếu không thì token không hợp lệ hoặc
        // đã hết hạn
        if (userIdStr == null) {
            throw new InvalidResetTokenException("Token không hợp lệ hoặc đã hết hạn!");
        }

        // Chuyển đổi userId từ String sang Long và tìm user trong cơ sở dữ liệu
        Long userId = Long.valueOf(userIdStr);

        // Tìm user theo id
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("Tài khoản không tồn tại!"));
        // Cập nhật mật khẩu mới cho user
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
        // kiểm tra user hiện tại có tồn tại hay không
        if (currentUser == null || currentUser.getId() == null) {
            throw new UserNotFoundException("Tài khoản không tồn tại!");
        }
        // trả về thông tin profile của user hiện tại
        return mapToResponse(currentUser);
    }

    @Override
    public AuthResponse refreshToken(String token) {
        return new AuthResponse(); // Not used anymore
    }

    @Override
    @Transactional
    /**
     * Vô hiệu hóa tài khoản: Cập nhật trạng thái user thành INACTIVE và enabled
     * thành false.
     */
    public void deactivateAccount(Long userId) {
        // Tìm user theo id
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("Tài khoản không tồn tại!"));
        // Cập nhật trạng thái user thành INACTIVE và enabled thành false
        user.setEnabled(false);
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    // ================= CÁC HÀM PRIVATE HỖ TRỢ =================

    private void updateBasicInfo(User user, UpdateProfileRequest request) {
        user.setPhone(request.getPhone());
        user.setBio(request.getBio());
        user.setBirthDate(request.getBirthDate());
    }

    private void handleAvatarUpdate(User user, String newAvatarUrl) {
        // Sử dụng Guard Clause: Nếu không có URL mới thì thoát hàm ngay lập tức, triệt
        // tiêu if lồng nhau
        if (newAvatarUrl == null || newAvatarUrl.isBlank()) {
            return;
        }

        // Khởi tạo Image nếu chưa có
        if (user.getAvatar() == null) {
            user.setAvatar(new Image());
        }
        user.getAvatar().setUrl(newAvatarUrl);

        // Ép Hibernate lưu xuống DB ngay lập tức để sinh ID cho Image mới
        userRepository.saveAndFlush(user);

        // Phát sự kiện
        ProfileImageConfirmEvent event = new ProfileImageConfirmEvent(
                this,
                user.getAvatar().getId(),
                newAvatarUrl);
        publisher.publishEvent(event);
    }

    private UserProfileResponse mapToResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .bio(user.getBio())
                .birthDate(user.getBirthDate() != null ? user.getBirthDate().toString() : null)
                .avatarUrl(user.getAvatar() != null ? user.getAvatar().getUrl() : null)
                .roles(user.getAuthorities() != null
                        ? user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()
                        : java.util.List.of())
                .build();
    }

    private ResponseCookie generateCookie(String tokenValue) {
        long maxAgeInSeconds = expireRtDay * 24L * 60 * 60;
        ResponseCookie springCookie = ResponseCookie.from("token", tokenValue)
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .maxAge(maxAgeInSeconds)
                .sameSite(sameSite)
                .domain(domain)
                .build();
        return springCookie;
    }

    private String generateToken(User user) {
        // Generate a refresh token using expireRtDay to serve as a session token
        return jwtService.generateAccessToken(user.getId(), (List<GrantedAuthority>) user.getAuthorities());
    }
}
