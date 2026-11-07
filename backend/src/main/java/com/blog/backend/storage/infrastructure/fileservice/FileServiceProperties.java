package com.blog.backend.storage.infrastructure.fileservice;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Connection to the shared file-service. Values come from FILE_SERVICE_URL / FILE_SERVICE_API_KEY
 * (backend/.env locally, environment variables in production).
 */
@Data
@Validated
@ConfigurationProperties(prefix = "file-service")
public class FileServiceProperties {

    @NotBlank(message = "file-service.base-url (FILE_SERVICE_URL) is required")
    private String baseUrl;

    /** The blog's key in file-service (API_KEY_BLOG there), sent as the x-api-key header. */
    @NotBlank(message = "file-service.api-key (FILE_SERVICE_API_KEY) is required")
    private String apiKey;
}
