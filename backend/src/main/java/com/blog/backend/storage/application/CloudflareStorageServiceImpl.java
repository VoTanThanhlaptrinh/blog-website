package com.blog.backend.storage.application;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.blog.backend.storage.api.dto.UpdateImagePrefixResponse;
import com.blog.backend.storage.api.dto.UploadPostResponse;
import com.blog.backend.storage.api.dto.UploadUrlRequest;
import com.blog.backend.storage.application.util.StorageUtils;
import com.blog.backend.storage.domain.constant.StorageConstants;
import com.blog.backend.storage.infrastructure.cloudflare.config.CloudflareR2Properties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudflareStorageServiceImpl implements StorageService {

    private final S3Client s3Client;
    private final CloudflareR2Properties properties;
    private final S3Presigner s3Presigner;

    /**
     * Copy file từ sourceKey sang destinationKey
     *
     * @param sourceKey      Đường dẫn file nguồn (ví dụ: "identity/temp/avatar/123.jpg")
     * @param destinationKey Đường dẫn file đích (ví dụ: "identity/avatar/123.jpg")
     * @return Đường dẫn URL công khai hoàn chỉnh (ví dụ:
     *         "https://cdn.domain.com/identity/avatar/123.jpg")
     */
    @Override
    public String copyFile(String sourceKey, String destinationKey) {
        if (sourceKey == null || sourceKey.isBlank() || destinationKey == null || destinationKey.isBlank()) {
            throw new IllegalArgumentException("Đường dẫn file nguồn và đích không được để trống");
        }

        String bucketName = properties.getBucket();

        try {
            // Thực hiện lệnh COPY file trong nội bộ Cloudflare R2 sang key đích
            CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                    .sourceBucket(bucketName)
                    .sourceKey(sourceKey)
                    .destinationBucket(bucketName)
                    .destinationKey(destinationKey)
                    .build();
            s3Client.copyObject(copyRequest);

            // Không cần xóa file gốc vì Cloudflare đã được cấu hình tự động xóa file có chứa "/temp" sau 1 ngày

            // Chuẩn hóa Base URL và ghép nối tạo ra URL hiển thị công khai hoàn chỉnh
            String baseUrl = properties.getPublicUrl();
            if (baseUrl != null && baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }

            return String.format("%s/%s", baseUrl, destinationKey);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Thao tác copy file trên Cloudflare R2 thất bại: " + e.getMessage(), e);
        }
    }

    public UploadPostResponse generatePresignedUrl(UploadUrlRequest request) throws Exception {
        if (!StorageConstants.ALLOWED_CONTENT_TYPES.contains(request.getContentType())) {
            throw new IllegalArgumentException("Loại file không hợp lệ");
        }

        // Định nghĩa Object Key
        String objectKey = StorageUtils.generateObjectKey(request.getFolder(), request.getFileName(),
                StorageConstants.TEMP_FOLDER_PREFIX);

        // Tạo PutObjectRequest
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .contentType(request.getContentType())
                .build();

        // Tạo yêu cầu Pre-sign cho PUT object (Hết hạn sau 15 phút)
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(putObjectRequest)
                .build();

        // Sinh URL
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        String presignedUrl = presignedRequest.url().toString();

        // Xây dựng URL công khai
        String baseUrl = properties.getPublicUrl();
        if (baseUrl != null && baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        String publicUrl = String.format("%s/%s", baseUrl, objectKey);

        return UploadPostResponse.builder()
                .uploadUrl(presignedUrl)
                .objectKey(objectKey)
                .formData(new HashMap<>())
                .publicUrl(publicUrl)
                .build();
    }

    /**
     * Cập nhật tiền tố (prefix/thư mục) hàng loạt cho danh sách URL ảnh trên R2.
     * Thường dùng khi chuyển ảnh tạm từ thư mục "temp/" sang thư mục lưu trữ chính
     * thức như "blog/" hoặc "avatar/".
     *
     * @param imageUrls    Danh sách URL/Key ảnh cần chuyển đổi
     * @param sourcePrefix Tiền tố nguồn (mặc định "temp/")
     * @param targetPrefix Tiền tố đích (mặc định "blog/")
     * @return Danh sách các URL công khai mới sau khi đã di chuyển thành công
     */
    @Override
    public UpdateImagePrefixResponse updateImagePrefixes(List<String> imageUrls, String sourcePrefix,
            String targetPrefix) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return new UpdateImagePrefixResponse(List.of());
        }

        String srcPrefix = sourcePrefix != null && !sourcePrefix.isBlank()
                ? sourcePrefix.trim()
                : StorageConstants.TEMP_FOLDER_PREFIX;
        if (!srcPrefix.endsWith("/")) {
            srcPrefix = srcPrefix + "/";
        }

        String tgtPrefix = targetPrefix != null && !targetPrefix.isBlank()
                ? targetPrefix.trim()
                : "blog/";
        if (!tgtPrefix.endsWith("/")) {
            tgtPrefix = tgtPrefix + "/";
        }

        String baseUrl = properties.getPublicUrl();
        if (baseUrl != null && baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        List<String> updatedUrls = new java.util.ArrayList<>();
        String bucketName = properties.getBucket();

        for (String urlOrKey : imageUrls) {
            if (urlOrKey == null || urlOrKey.isBlank()) {
                continue;
            }

            String key = urlOrKey.trim();
            if (baseUrl != null && key.startsWith(baseUrl)) {
                key = key.substring(baseUrl.length());
                if (key.startsWith("/")) {
                    key = key.substring(1);
                }
            } else if (key.contains("://")) {
                int pathIndex = key.indexOf('/', key.indexOf("://") + 3);
                if (pathIndex != -1) {
                    key = key.substring(pathIndex + 1);
                }
            }

            String destinationKey;
            if (key.startsWith(srcPrefix)) {
                destinationKey = tgtPrefix + key.substring(srcPrefix.length());
            } else if (key.startsWith("temp/")) {
                destinationKey = tgtPrefix + key.substring("temp/".length());
            } else {
                destinationKey = tgtPrefix + key;
            }

            try {
                CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                        .sourceBucket(bucketName)
                        .sourceKey(key)
                        .destinationBucket(bucketName)
                        .destinationKey(destinationKey)
                        .build();
                s3Client.copyObject(copyRequest);

                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();
                s3Client.deleteObject(deleteRequest);

                String finalPublicUrl = String.format("%s/%s", baseUrl, destinationKey);
                updatedUrls.add(finalPublicUrl);
            } catch (Exception e) {
                log.error("Lỗi khi chuyển prefix từ key {} sang {}: {}", key, destinationKey, e.getMessage());
                throw new RuntimeException("Không thể cập nhật prefix cho file: " + key + ". Lỗi: " + e.getMessage(),
                        e);
            }
        }

        return new UpdateImagePrefixResponse(updatedUrls);
    }
}
