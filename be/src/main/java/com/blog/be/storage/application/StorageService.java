package com.blog.be.storage.application;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public interface StorageService {
    /**
     * Upload a file and return its public URL.
     *
     * @param file the multipart file to upload
     * @return the public URL of the uploaded file
     */
    String uploadFile(MultipartFile file);

    /**
     * Upload a file from an input stream and return its public URL.
     *
     * @param inputStream the input stream
     * @param originalFilename the original name of the file
     * @param contentType the MIME type of the file
     * @param contentLength the size of the file
     * @return the public URL of the uploaded file
     */
    String uploadFile(InputStream inputStream, String originalFilename, String contentType, long contentLength);

    /**
     * Delete a file by its URL or key.
     *
     * @param fileUrl the public URL or key of the file
     */
    void deleteFile(String fileUrl);
}
