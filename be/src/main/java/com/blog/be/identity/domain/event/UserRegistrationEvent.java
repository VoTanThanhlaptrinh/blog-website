package com.blog.be.identity.domain.event;

import lombok.Builder;

@Builder
public record UserRegistrationEvent(String email, String token) {
}
