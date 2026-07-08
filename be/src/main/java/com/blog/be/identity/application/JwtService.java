package com.blog.be.identity.application;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;

@RequiredArgsConstructor
@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private  String issuerUri;
    @Value("${identity.expire-at-minute}")
    private int expireAtMinute;
    @Value("${identity.expire-rt-day}")
    private int expireRtDay;

    public Jwt getCurrentJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken();
        }
        return null;
    }

    public String getCurrentUserId() {
        Jwt jwt = getCurrentJwt();
        return jwt != null ? jwt.getSubject() : null;
    }
    public boolean isTokenFresh(int maxAge) {
        Jwt jwt = getCurrentJwt();
        if (jwt == null || jwt.getIssuedAt() == null) {
            return false;
        }

        Instant issuedAt = jwt.getIssuedAt();
        Instant threshold = Instant.now().minusSeconds(maxAge *  60L);

        return issuedAt.isAfter(threshold);
    }
    /**
     * Hàm kiểm tra token có hợp lệ và còn hạn hay không
     * @param token Chuỗi JWT gốc
     * @return true nếu hợp lệ và còn hạn, false nếu hỏng hoặc hết hạn
     */
    public boolean isTokenValid(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return true;
        } catch (JwtValidationException e) {
            System.out.println("Token không hợp lệ: " + e.getMessage());
            return false;
        } catch (JwtException e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getClaim(String claimName, Class<T> claimType) {
        Jwt jwt = getCurrentJwt();
        if (jwt == null) {
            return null;
        }

        Object claim = jwt.getClaim(claimName);
        if (claimType.isInstance(claim)) {
            return (T) claim;
        }
        return null;
    }

    public <T> T getClaim(String token, String claimName, Class<T> claimType) {
        Jwt jwt = jwtDecoder.decode(token);
        if (jwt == null) {
            return null;
        }

        Object claim = jwt.getClaim(claimName);
        if (claimType.isInstance(claim)) {
            return (T) claim;
        }
        return null;
    }

    public boolean hasRequiredScopes(List<String> requiredScopes) {
        Jwt jwt = getCurrentJwt();
        if (jwt == null) {
            return false;
        }

        // Scopes can be in 'scope' claim (space-separated) or 'scp' claim (array)
        String scopeString = jwt.getClaimAsString("scope");
        List<String> tokenScopes;

        if (scopeString != null) {
            tokenScopes = List.of(scopeString.split(" "));
        } else {
            tokenScopes = jwt.getClaimAsStringList("scp");
        }

        if (tokenScopes == null) {
            return false;
        }

        return new HashSet<>(tokenScopes).containsAll(requiredScopes);
    }
    public String generateAccessToken(Long userId, List<GrantedAuthority> roles) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuerUri)
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiresAt(now.plus(expireAtMinute, ChronoUnit.MINUTES))
                .claim("userId", userId)
                .claim("roles", roles)
                .claim("type", "access_token") // Đánh dấu loại token
                .build();

        return signToken(claims);
    }
    public String generateRefreshToken(Long userId) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuerUri)
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiresAt(now.plus(expireRtDay, ChronoUnit.DAYS))
                .claim("userId", userId)
                .claim("type", "refresh_token")
                .build();

        return signToken(claims);
    }

    public String generateActiveToken(Long userId) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuerUri)
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.DAYS))
                .claim("type", "active_token")
                .build();
        return signToken(claims);
    }

    private String signToken(JwtClaimsSet claims) {
        // Cố định thuật toán ký là HS256
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        // Tiến hành ký
        return this.jwtEncoder
                .encode(JwtEncoderParameters.from(jwsHeader, claims))
                .getTokenValue();
    }
}
