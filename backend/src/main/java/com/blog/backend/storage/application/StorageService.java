package com.blog.be.storage.application;

import com.blog.be.storage.api.dto.UpdateImagePrefixResponse;
import com.blog.be.storage.api.dto.UploadPostResponse;
import com.blog.be.storage.api.dto.UploadUrlRequest;
import java.util.List;

public interface StorageService {
    UploadPostResponse generatePresignedUrl(UploadUrlRequest request) throws Exception;
    String confirmAndActivateFile(String tempKey);
    String confirmAndActivateFile(String tempKey, String prefix);
    UpdateImagePrefixResponse updateImagePrefixes(List<String> imageUrls, String sourcePrefix, String targetPrefix);
}
