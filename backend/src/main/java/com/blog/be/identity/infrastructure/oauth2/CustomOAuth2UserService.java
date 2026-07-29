package com.blog.be.identity.infrastructure.oauth2;

import com.blog.be.identity.domain.entity.User;
import com.blog.be.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findUserByEmail(email);
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
        } else {
            // Auto create new user if not exists
            user = User.builder()
                    .email(email)
                    // Generate a random password since they use OAuth2
                    .password(UUID.randomUUID().toString())
                    .enabled(true)
                    .build();
            user = userRepository.saveAndFlush(user);
        }

        // Add our DB userId to the attributes so the success handler can use it
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("db_user_id", user.getId());

        return new DefaultOAuth2User(oAuth2User.getAuthorities(), attributes, "email");
    }
}
