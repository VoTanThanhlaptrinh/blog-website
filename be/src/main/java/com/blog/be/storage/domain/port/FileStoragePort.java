package com.blog.be.storage.domain.port;

import java.io.InputStream;

public interface FileStoragePort {
    /**
     * Upload a file from an input stream and return its unique key/URL.
     *
     * @param inputStream the input stream
     * @param originalFilename the original name of the file
     * @param contentType the MIME type of the file
     * @param contentLength the size of the file
     * @return the unique key or public URL of the uploaded file
     */
    String uploadFile(InputStream inputStream, String originalFilename, String contentType, long contentLength);

    /**
     * Delete a file by its unique key.
     *
     * @param fileKey the unique key of the file
     */
    void deleteFile(String fileKey);
}
