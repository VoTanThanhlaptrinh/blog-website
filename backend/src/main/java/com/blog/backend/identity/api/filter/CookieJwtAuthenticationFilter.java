package com.blog.backend.identity.api.filter;

import com.blog.backend.identity.application.JwtService;
import com.blog.backend.identity.domain.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j // Thay thế System.out.println
@Component
@RequiredArgsConstructor
public class CookieJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = extractTokenFromCookie(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                if (jwtService.isTokenValid(token)) {
                    Object userIdClaim = jwtService.getClaim(token, "userId", Object.class);

                    if (userIdClaim != null) {
                        Long userId = userIdClaim instanceof Number
                                ? ((Number) userIdClaim).longValue()
                                : Long.valueOf(userIdClaim.toString());

                        // Dùng findUserByIdWithRoles thay vì findById để kéo kèm theo userRoles, tránh LazyInitializationException
                        userRepository.findUserByIdWithRoles(userId).ifPresent(user -> {

                            List<?> rolesList = jwtService.getClaim(token, "roles", List.class);
                            List<SimpleGrantedAuthority> authorities = List.of();

                            if (rolesList != null) {
                                authorities = rolesList.stream()
                                        // Đảm bảo role có prefix ROLE_ nếu hệ thống của bạn yêu cầu
                                        .map(role -> {
                                            String roleStr = role.toString();
                                            if (!roleStr.startsWith("ROLE_")) {
                                                roleStr = "ROLE_" + roleStr;
                                            }
                                            return new SimpleGrantedAuthority(roleStr);
                                        })
                                        .collect(Collectors.toList());
                            }

                            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    authorities);

                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                        });
                    }
                }
            } catch (Exception e) {
                // Ghi log lỗi thay vì in ra màn hình console
                log.error("Lỗi xác thực Cookie JWT trong Filter: {}", e.getMessage());
                throw e;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}