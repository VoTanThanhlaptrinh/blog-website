package com.blog.be.identity.application;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;

@Service
public class TokenService {
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
    public boolean isTokenFresh(int maxAgeMinutes) {
        Jwt jwt = getCurrentJwt();
        if (jwt == null || jwt.getIssuedAt() == null) {
            return false;
        }

        Instant issuedAt = jwt.getIssuedAt();
        Instant threshold = Instant.now().minusSeconds(maxAgeMinutes * 60L);

        return issuedAt.isAfter(threshold);
    }
    @SuppressWarnings("unchecked")
    public <T> T getClaim(String claimName, Class<T> claimType) {
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
    public String signToken(String username) throws JOSEException {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

        SignedJWT signedJWT = new SignedJWT(header, claimsSet);

        JWSSigner signer = new MACSigner(SECRET_KEY.getBytes());
        signedJWT.sign(signer);

        // 5. Chuyển đổi thành chuỗi String để trả về cho Client
        return signedJWT.serialize();
    }
}
