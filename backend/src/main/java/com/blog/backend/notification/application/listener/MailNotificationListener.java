package com.blog.backend.notification.application.listener;

import com.blog.backend.identity.domain.event.UserRegistrationEvent;
import com.blog.backend.identity.infrastructure.utils.StringHelper;
import com.blog.backend.notification.application.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailNotificationListener {
    private final MailService mailSender;

    @Async
    @EventListener
    public void handleUserRegistrationEvent(UserRegistrationEvent event) {
        String mailBody = String.format(StringHelper.mailBodyForActivingAccount(),event.getEmail(),event.getToken());
        mailSender.sendMail(event.getEmail(), "Kích hoạt tài khoản", mailBody);
    }
}
