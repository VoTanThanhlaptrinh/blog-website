package com.blog.be.identity.infrastructure.config;

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

import com.blog.be.identity.domain.repository.UserRepository;
import com.blog.be.identity.infrastructure.oauth2.CustomOAuth2UserService;
import com.blog.be.identity.infrastructure.oauth2.OAuth2AuthenticationSuccessHandler;

import lombok.AllArgsConstructor;

/**
 * Cấu hình bảo mật hệ thống (Spring Security + OAuth2 + JWT Resource Server).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {
        private final UserRepository userRepository;
        private final Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter;
        private final CustomOAuth2UserService customOAuth2UserService;
        private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

        /**
         * Cấu hình Chuỗi lọc Bảo mật (Security Filter Chain):
         * 1. Vô hiệu hóa CSRF do hệ thống dùng Token Stateless.
         * 2. Quản lý phiên Session STATELESS (không dùng Session trên Server).
         * 3. Phân quyền Request: Mở công khai các đường dẫn Đăng nhập, OAuth2, và các API GET xem bài viết.
         * 4. Tích hợp OAuth2 Login (Google/Facebook) kèm xử lý thành công Custom Success Handler.
         * 5. Cấu hình JWT Resource Server tự động xác thực Bearer Token trong Request Header.
         */
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                return http
                                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/login/**", "/oauth2/**").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/v1/blogs", "/api/v1/blogs/**")
                                                .permitAll()
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
        public UserDetailsService userDetailsService() {
                return username -> userRepository.findUserByEmail(username).orElseThrow();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

}
