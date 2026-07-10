package com.blog.be.storage.domain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {
    private ModuleStorageProperties identity;
    private ModuleStorageProperties blog;

    @Data
    public static class ModuleStorageProperties {
        private List<String> allowedContentTypes;
        private String tempFolderPrefix;
        private String region;
        private String service;
        private String algorithm;
        private long maxFileSizeBytes;
    }
}
