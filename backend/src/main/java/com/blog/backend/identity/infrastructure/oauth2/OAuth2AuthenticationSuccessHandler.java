package com.blog.be.identity.infrastructure.oauth2;

import com.blog.be.identity.application.JwtService;
import com.blog.be.identity.domain.entity.User;
import com.blog.be.identity.domain.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

/**
 * Xử lý sự kiện đăng nhập OAuth2 thành công (Google / Facebook).
 * Luồng hoạt động:
 * 1. Lấy thông tin User đã được lưu/đồng bộ từ CustomOAuth2UserService.
 * 2. Tạo Refresh Token và ghi vào HttpOnly Cookie an toàn để chống XSS.
 * 3. Tạo Access Token tạm thời.
 * 4. Chuyển hướng (Redirect) về Client Frontend kèm Access Token trên Query Parameter để Client lưu phiên.
 */
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Value("${identity.expire-rt-day}")
    private Integer expireRtDay;

    @Value("${app.cookie.secure}")
    private boolean secureCookie;

    @Value("${app.cookie.same-site}")
    private String sameSite;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Long userId = oAuth2User.getAttribute("db_user_id");

        // Clear the OAuth2 cookies
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found after OAuth2 login"));

        String refreshToken = jwtService.generateRefreshToken(currentUser.getId());

        long maxAgeInSeconds = expireRtDay * 24L * 60 * 60;
        ResponseCookie springCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .maxAge(maxAgeInSeconds)
                .sameSite(sameSite)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, springCookie.toString());

        String accessToken = jwtService.generateAccessToken(currentUser.getId(), 
                (List<GrantedAuthority>) currentUser.getAuthorities());

        // We redirect to frontend and pass the access token in URL
        // (Refresh token is in HttpOnly cookie)
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/redirect")
                .queryParam("accessToken", accessToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
