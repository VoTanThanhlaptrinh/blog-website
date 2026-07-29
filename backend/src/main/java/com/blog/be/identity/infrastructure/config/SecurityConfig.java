package com.blog.be.identity.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.blog.be.identity.domain.repository.UserRepository;
import com.blog.be.identity.infrastructure.oauth2.CustomOAuth2UserService;
import com.blog.be.identity.infrastructure.oauth2.OAuth2AuthenticationSuccessHandler;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Cấu hình bảo mật hệ thống (Spring Security + OAuth2 + JWT Resource Server).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
        private final UserRepository userRepository;
        private final Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter;
        private final CustomOAuth2UserService customOAuth2UserService;
        private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

        @Value("${app.frontend.url:http://localhost:4200}")
        private String frontendUrl;

        /**
         * Cấu hình Chuỗi lọc Bảo mật (Security Filter Chain):
         * 1. Cấu hình CORS mở quyền truy cập cho frontend (localhost:4200).
         * 2. Vô hiệu hóa CSRF do hệ thống dùng Token Stateless.
         * 3. Quản lý phiên Session STATELESS (không dùng Session trên Server).
         * 4. Phân quyền Request: Mở công khai các đường dẫn Đăng nhập, OAuth2, và các
         * API GET xem bài viết.
         * 5. Tích hợp OAuth2 Login (Google/Facebook) kèm xử lý thành công Custom
         * Success Handler.
         * 6. Cấu hình JWT Resource Server tự động xác thực Bearer Token trong Request
         * Header.
         */
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                return http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register",
                                                                "/api/v1/auth/activeAccount",
                                                                "/api/v1/auth/forgot-password",
                                                                "/api/v1/auth/verify-otp",
                                                                "/api/v1/auth/reset-password", "/api/v1/auth/refresh",
                                                                "/api/v1/auth/login/social")
                                                .permitAll()
                                                .requestMatchers("/login/**", "/oauth2/**").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/v1/blogs", "/api/v1/blogs/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/v1/stats/**").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/v1/comments/blog/**").permitAll()
                                                .anyRequest().authenticated())
                                .oauth2Login(oauth2 -> oauth2
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService))
                                                .successHandler(oAuth2AuthenticationSuccessHandler))
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt
                                                                .jwtAuthenticationConverter(
                                                                                jwtAuthenticationConverter)))
                                .build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOriginPatterns(List.of(frontendUrl));
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setExposedHeaders(List.of("Authorization", "Set-Cookie", "Location"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        @Bean
        public UserDetailsService userDetailsService() {
                return username -> userRepository.findUserByEmail(username).orElseThrow();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

}
