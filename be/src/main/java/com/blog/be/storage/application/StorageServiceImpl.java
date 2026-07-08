package com.blog.be.storage.application;

import com.blog.be.storage.domain.port.FileStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final FileStoragePort fileStoragePort;

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            return uploadFile(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize()
            );
        } catch (IOException e) {
            log.error("Failed to read file input stream", e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String originalFilename, String contentType, long contentLength) {
        return fileStoragePort.uploadFile(inputStream, originalFilename, contentType, contentLength);
    }

    @Override
    public void deleteFile(String fileUrl) {
        String key = extractKey(fileUrl);
        fileStoragePort.deleteFile(key);
    }

    private String extractKey(String fileUrl) {
        if (fileUrl != null && fileUrl.contains("/")) {
            return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        }
        return fileUrl;
    }
}
