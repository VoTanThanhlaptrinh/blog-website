package com.blog.be.storage.domain.constant;

import java.util.List;

public final class StorageConstants {

    private StorageConstants() {
        // Private constructor to prevent instantiation
    }

    public static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    
    public static final String TEMP_FOLDER_PREFIX = "temp/";
    public static final String REGION = "auto";
    public static final String SERVICE = "s3";
    public static final String ALGORITHM = "AWS4-HMAC-SHA256";
    public static final long MAX_FILE_SIZE_BYTES = 5242880L;
}
