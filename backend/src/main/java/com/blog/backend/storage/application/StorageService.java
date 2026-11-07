package com.blog.backend.storage.application;

import com.blog.backend.storage.api.dto.UploadPostResponse;
import com.blog.backend.storage.api.dto.UploadUrlRequest;

import java.util.Collection;
import java.util.Map;

public interface StorageService {

    /** Asks file-service for a presigned PUT URL; the response keeps the shape the frontend already uses. */
    UploadPostResponse createUploadUrl(UploadUrlRequest request);

    /**
     * Confirms every blog temp file referenced by the given URLs (others are ignored) and returns
     * a map from each temp URL to its permanent URL.
     */
    Map<String, String> confirmTempUrls(Collection<String> urls);
}
