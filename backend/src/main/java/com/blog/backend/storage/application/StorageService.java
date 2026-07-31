package com.blog.backend.storage.application;

import com.blog.backend.storage.api.dto.UpdateImagePrefixResponse;
import com.blog.backend.storage.api.dto.UploadPostResponse;
import com.blog.backend.storage.api.dto.UploadUrlRequest;
import java.util.List;

public interface StorageService {
    UploadPostResponse generatePresignedUrl(UploadUrlRequest request) throws Exception;
    String copyFile(String sourceKey, String destinationKey);
    UpdateImagePrefixResponse updateImagePrefixes(List<String> imageUrls, String sourcePrefix, String targetPrefix);
}
