package com.blog.backend.storage.infrastructure.cloudflare.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@EnableConfigurationProperties(CloudflareR2Properties.class)
@RequiredArgsConstructor
public class CloudflareR2Config {

    private final CloudflareR2Properties properties;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(getEndpointUrl()))
                .credentialsProvider(StaticCredentialsProvider.create(getCredential()))
                .region(Region.of("auto"))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {

        return S3Presigner.builder()
                .endpointOverride(URI.create(getEndpointUrl()))
                .region(Region.of("auto")) // R2 luôn dùng region "auto"
                .credentialsProvider(StaticCredentialsProvider.create(getCredential()))
                .build();
    }

    private String getAccountId() {
        return properties.getAccountId() != null ? properties.getAccountId() : "";

    }
    private AwsBasicCredentials getCredential() {
        return AwsBasicCredentials.create(
                properties.getAccessKey() != null ? properties.getAccessKey() : "",
                properties.getSecretKey() != null ? properties.getSecretKey() : ""
        );
    }
    private String getEndpointUrl() {
        return String.format("https://%s.r2.cloudflarestorage.com", getAccountId());
    }
}
