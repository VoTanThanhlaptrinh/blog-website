package com.blog.be.storage.infrastructure.cloudflare;

import com.blog.be.storage.domain.port.FileStoragePort;
import com.blog.be.storage.infrastructure.cloudflare.config.CloudflareR2Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CloudflareR2StorageAdapter implements FileStoragePort {

    private final S3Client s3Client;
    private final CloudflareR2Properties properties;

    @Override
    public String uploadFile(InputStream inputStream, String originalFilename, String contentType, long contentLength) {
        String extension = StringUtils.getFilenameExtension(originalFilename);
        String uniqueFileName = UUID.randomUUID().toString() + (extension != null ? "." + extension : "");

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(uniqueFileName)
                .contentType(contentType)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));
        return uniqueFileName;
    }

    @Override
    public void deleteFile(String fileKey) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(fileKey)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
        log.info("Deleted file with key: {}", fileKey);
    }
}
