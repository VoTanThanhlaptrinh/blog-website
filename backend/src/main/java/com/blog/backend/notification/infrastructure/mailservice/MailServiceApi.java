package com.blog.backend.notification.infrastructure.mailservice;

import com.blog.backend.notification.infrastructure.mailservice.dto.SendMailRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/** Declarative HTTP client for mail-service (only the endpoint the blog uses). */
@HttpExchange("/mails")
public interface MailServiceApi {

    /**
     * Sends one plain-text email synchronously. 201 means SMTP accepted it; any other status is turned
     * into a {@code MailDeliveryException} by {@link MailServiceClientConfig}, so the response body is not needed.
     */
    @PostExchange("/send")
    void send(@RequestBody SendMailRequest request);
}
