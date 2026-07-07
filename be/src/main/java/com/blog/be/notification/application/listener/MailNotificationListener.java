package com.blog.be.notification.application.listener;

import com.blog.be.identity.domain.event.UserRegistrationEvent;
import com.blog.be.identity.infrastructure.utils.StringHelper;
import com.blog.be.notification.application.service.MailService;
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
        String mailBody = String.format(StringHelper.mailBodyForActivingAccount(),event.email(),event.token());
        mailSender.sendMail(event.email(), "Kích hoạt tài khoản", mailBody);
    }
}
