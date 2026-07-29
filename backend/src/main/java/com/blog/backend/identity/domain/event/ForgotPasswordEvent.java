package com.blog.backend.identity.domain.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ForgotPasswordEvent extends ApplicationEvent {
    private final String email;
    private final String otp;

    public ForgotPasswordEvent(Object source, String email, String otp) {
        super(source);
        this.email = email;
        this.otp = otp;
    }
}
