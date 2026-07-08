package com.blog.be.identity.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
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
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        // Set our custom authorities converter that combines scopes and roles
        converter.setJwtGrantedAuthoritiesConverter(new CustomGrantedAuthoritiesConverter());

        // Use 'sub' claim as the principal name (default behavior)
        converter.setPrincipalClaimName("sub");

        return converter;
    }

    /**
     * Custom converter that extracts authorities from multiple JWT claims:
     * - Standard 'scope' claim (space-separated string)
     * - Keycloak 'realm_access.roles' claim (nested object)
     * - Auth0 'permissions' claim (array)
     */
    static class CustomGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

        private final JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();

        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            // Get default authorities from 'scope' claim (prefixed with SCOPE_)
            Collection<GrantedAuthority> defaultAuthorities = defaultConverter.convert(jwt);

            // Extract roles from Keycloak's realm_access claim
            Collection<GrantedAuthority> keycloakRoles = extractKeycloakRoles(jwt);

            // Extract permissions from Auth0's permissions claim
            Collection<GrantedAuthority> auth0Permissions = extractAuth0Permissions(jwt);

            // Combine all authorities into a single collection
            return Stream.of(defaultAuthorities, keycloakRoles, auth0Permissions)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
        }

        @SuppressWarnings("unchecked")
        private Collection<GrantedAuthority> extractKeycloakRoles(Jwt jwt) {
            // Keycloak stores roles in: realm_access.roles
            var realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess == null) {
                return Collections.emptyList();
            }

            var roles = (List<String>) realmAccess.get("roles");
            if (roles == null) {
                return Collections.emptyList();
            }

            // Prefix with ROLE_ to work with hasRole() checks
            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList());
        }

        private Collection<GrantedAuthority> extractAuth0Permissions(Jwt jwt) {
            // Auth0 stores permissions in a 'permissions' claim array
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
