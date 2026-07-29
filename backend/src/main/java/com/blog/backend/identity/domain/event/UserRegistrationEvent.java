package com.blog.be.identity.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserRegistrationEvent extends ApplicationEvent {
    private final String email;
    private final String token;

    public UserRegistrationEvent(Object source, String email, String token) {
        super(source);
        this.email = email;
        this.token = token;
    }
}
