package com.blog.be.identity.infrastructure.config;

import com.blog.be.identity.domain.entity.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class JwtAuthenticationConverterConfig {

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return new Converter<Jwt, AbstractAuthenticationToken>() {
            private final CustomGrantedAuthoritiesConverter authoritiesConverter = new CustomGrantedAuthoritiesConverter();

            @Override
            public AbstractAuthenticationToken convert(Jwt jwt) {
                Collection<GrantedAuthority> authorities = authoritiesConverter.convert(jwt);

                Long userId = null;
                if (jwt.getSubject() != null) {
                    try {
                        userId = Long.valueOf(jwt.getSubject());
                    } catch (NumberFormatException ignored) {}
                }

                User user = User.builder()
                        .id(userId)
                        .email(jwt.getClaimAsString("email"))
                        .enabled(true)
                        .build();

                return new UsernamePasswordAuthenticationToken(user, jwt.getTokenValue(), authorities);
            }
        };
    }

    static class CustomGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        private final JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();

        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            Collection<GrantedAuthority> defaultAuthorities = defaultConverter.convert(jwt);
            Collection<GrantedAuthority> keycloakRoles = extractKeycloakRoles(jwt);
            Collection<GrantedAuthority> auth0Permissions = extractAuth0Permissions(jwt);

            return Stream.of(defaultAuthorities, keycloakRoles, auth0Permissions)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
        }

        @SuppressWarnings("unchecked")
        private Collection<GrantedAuthority> extractKeycloakRoles(Jwt jwt) {
            var realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess == null) {
                return Collections.emptyList();
            }

            var roles = (List<String>) realmAccess.get("roles");
            if (roles == null) {
                return Collections.emptyList();
            }

            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList());
        }

        private Collection<GrantedAuthority> extractAuth0Permissions(Jwt jwt) {
            List<String> permissions = jwt.getClaimAsStringList("permissions");
            if (permissions == null) {
                return Collections.emptyList();
            }

            return permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
    }
}
