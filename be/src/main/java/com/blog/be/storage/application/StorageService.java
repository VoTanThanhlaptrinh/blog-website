package com.blog.be.storage.application;

import com.blog.be.storage.api.dto.UploadPostResponse;
import com.blog.be.storage.api.dto.UploadUrlRequest;
import com.blog.be.storage.api.dto.UploadUrlResponse;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public interface StorageService {
    UploadPostResponse generatePresignedUrl(UploadUrlRequest request) throws Exception;
    String confirmAndActivateFile(String tempKey);
}
