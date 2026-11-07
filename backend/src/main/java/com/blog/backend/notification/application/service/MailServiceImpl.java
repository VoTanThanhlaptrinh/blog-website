package com.blog.backend.notification.application.service;

import com.blog.backend.notification.domain.exception.MailDeliveryException;
import com.blog.backend.notification.infrastructure.mailservice.MailServiceApi;
import com.blog.backend.notification.infrastructure.mailservice.dto.SendMailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

/** Sends email through the shared mail-service (plain text only, no direct SMTP). */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {
    private final MailServiceApi mailServiceApi;

    @Override
    public void sendMail(String to, String subject, String body) {
        try {
            mailServiceApi.send(new SendMailRequest(to, subject, body));
            log.info("Email sent via mail-service to {}", to);
        } catch (MailDeliveryException e) {
            // Already logged with the status/body by MailServiceClientConfig.
            throw e;
        } catch (RestClientException e) {
            // Connection refused / timeout etc.
            log.error("Cannot reach mail-service while sending email to {}: {}", to, e.getMessage());
            throw new MailDeliveryException("Không thể kết nối tới mail-service.", e);
        }
    }
}
