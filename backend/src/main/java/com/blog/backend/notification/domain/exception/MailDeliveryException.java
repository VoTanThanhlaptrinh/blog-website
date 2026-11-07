package com.blog.backend.notification.domain.exception;

/** An email could not be handed to mail-service or mail-service failed to deliver it. */
public class MailDeliveryException extends RuntimeException {
    public MailDeliveryException(String message) {
        super(message);
    }

    public MailDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
