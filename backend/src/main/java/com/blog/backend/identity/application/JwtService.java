package com.blog.backend.identity.application;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class JwtService {

    @Value("${jwt.secret:defaultSecretKeyThatIsAtLeast32BytesLongForHS256Algorithm}")
    private String secretKey;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:http://localhost:8080}")
    private String issuerUri;

    @Value("${identity.expire-at-minute:60}")
    private int expireAtMinute;

    @Value("${identity.expire-rt-day:7}")
    private int expireRtDay;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Hàm kiểm tra token có hợp lệ và còn hạn hay không
     * @param token Chuỗi JWT gốc
     * @return true nếu hợp lệ và còn hạn, false nếu hỏng hoặc hết hạn
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (SignatureException | MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            System.out.println("Token không hợp lệ: " + e.getMessage());
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getClaim(String token, String claimName, Class<T> claimType) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

            Object claim = claims.get(claimName);
            if (claimType.isInstance(claim)) {
                return (T) claim;
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi lấy claim " + claimName + ": " + e.getMessage());
        }
        return null;
    }

    public String generateAccessToken(Long userId, List<GrantedAuthority> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (expireRtDay * 24L * 60 * 60 * 1000));

        List<String> roleNames = roles.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setIssuer(issuerUri)
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("userId", userId)
                .claim("roles", roleNames)
                .claim("type", "access_token")
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (expireRtDay * 24L * 60 * 60 * 1000));

        return Jwts.builder()
                .setIssuer(issuerUri)
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("userId", userId)
                .claim("type", "refresh_token")
                .signWith(getSigningKey())
                .compact();
    }

    public String generateActiveToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (24L * 60 * 60 * 1000)); // 1 day

        return Jwts.builder()
                .setIssuer(issuerUri)
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("type", "active_token")
                .signWith(getSigningKey())
                .compact();
    }
}
