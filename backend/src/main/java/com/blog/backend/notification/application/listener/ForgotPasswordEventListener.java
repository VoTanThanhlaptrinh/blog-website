package com.blog.be.notification.application.listener;

import com.blog.be.identity.domain.event.ForgotPasswordEvent;
import com.blog.be.notification.application.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ForgotPasswordEventListener {
    private final MailService mailService;

    @Async
    @EventListener
    public void handleForgotPasswordEvent(ForgotPasswordEvent event) {
        String to = event.getEmail();
        String subject = "Mã xác thực Quên Mật Khẩu";
        String content = com.blog.be.identity.infrastructure.utils.StringHelper.mailBodyForForgotPassword(to, event.getOtp());
        
        mailService.sendMail(to, subject, content);
    }
}
