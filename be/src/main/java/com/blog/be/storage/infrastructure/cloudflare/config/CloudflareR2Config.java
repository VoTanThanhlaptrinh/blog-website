package com.blog.be.storage.infrastructure.cloudflare.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class CloudflareR2Config {

    private final CloudflareR2Properties properties;

    @Bean
    public S3Client s3Client() {
        String accountId = properties.getAccountId() != null ? properties.getAccountId() : "";
        String endpointUrl = String.format("https://%s.r2.cloudflarestorage.com", accountId);

        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                properties.getAccessKey() != null ? properties.getAccessKey() : "",
                properties.getSecretKey() != null ? properties.getSecretKey() : ""
        );

        return S3Client.builder()
                .endpointOverride(URI.create(endpointUrl))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of("auto"))
                .build();
    }
}
