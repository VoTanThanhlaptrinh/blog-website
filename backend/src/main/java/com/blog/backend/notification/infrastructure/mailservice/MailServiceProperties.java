package com.blog.backend.notification.infrastructure.mailservice;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Connection to the shared mail-service. Values come from MAIL_SERVICE_URL / MAIL_SERVICE_API_KEY
 * (backend/.env locally, environment variables in production).
 */
@Data
@Validated
@ConfigurationProperties(prefix = "mail-service")
public class MailServiceProperties {

    /** Base URL including the /api prefix, e.g. http://localhost:4000/api. */
    @NotBlank(message = "mail-service.base-url (MAIL_SERVICE_URL) is required")
    private String baseUrl;

    /** API key created in mail-service (scope mail:send), sent as "Authorization: Bearer ...". */
    @NotBlank(message = "mail-service.api-key (MAIL_SERVICE_API_KEY) is required")
    private String apiKey;

    /** Written to created_by / audit trail in mail-service (X-Actor header). */
    @NotBlank
    private String actor = "blog-backend";
}
