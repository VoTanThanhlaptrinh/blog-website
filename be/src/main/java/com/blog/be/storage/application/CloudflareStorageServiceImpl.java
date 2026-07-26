package com.blog.be.storage.application;

import com.blog.be.storage.api.dto.UploadPostResponse;
import com.blog.be.storage.api.dto.UploadUrlRequest;
import com.blog.be.storage.infrastructure.cloudflare.config.CloudflareR2Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import com.blog.be.storage.application.util.AwsSignatureUtils;
import com.blog.be.storage.application.util.StorageUtils;
import com.blog.be.storage.domain.constant.StorageConstants;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudflareStorageServiceImpl implements StorageService {

    private final S3Client s3Client;
    private final CloudflareR2Properties properties;

    /**
     * Di chuyển file từ vùng temp ra thư mục gốc chính thức bằng cách loại bỏ tiền tố "temp/"
     * * @param tempKey Đường dẫn file tạm (ví dụ: "temp/avatar/uuid-filename.png")
     * @return Đường dẫn URL công khai hoàn chỉnh (ví dụ: "https://cdn.domain.com/avatar/uuid-filename.png")
     */
    public String confirmAndActivateFile(String tempKey) {
        return confirmAndActivateFile(tempKey, StorageConstants.TEMP_FOLDER_PREFIX);
    }

    public String confirmAndActivateFile(String tempKey, String prefix) {
        // 1. Kiểm tra tính hợp lệ của key đầu vào
        if (tempKey == null || !tempKey.startsWith(prefix)) {
            throw new IllegalArgumentException("Đường dẫn file tạm không hợp lệ hoặc không thuộc vùng xử lý");
        }

        // 2. Sử dụng Regex ^prefix để chỉ xóa chữ prefix ở đầu chuỗi
        String permanentKey = tempKey.replaceFirst("^" + prefix, "");
        String bucketName = properties.getBucket();

        try {
            // 3. Thực hiện lệnh COPY file trong nội bộ Cloudflare R2 sang key mới sạch sẽ hơn
            CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                    .sourceBucket(bucketName)
                    .sourceKey(tempKey)
                    .destinationBucket(bucketName)
                    .destinationKey(permanentKey)
                    .build();
            s3Client.copyObject(copyRequest);

            // 4. Xóa file ở vùng tạm ngay sau khi copy thành công để tránh rác dữ liệu
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(tempKey)
                    .build();
            s3Client.deleteObject(deleteRequest);

            // 5. Chuẩn hóa Base URL và ghép nối tạo ra URL hiển thị công khai hoàn chỉnh
            String baseUrl = properties.getPublicUrl();
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }

            return String.format("%s/%s", baseUrl, permanentKey);

        } catch (Exception e) {
            throw new RuntimeException("Thao tác dịch chuyển cấu trúc file trên Cloudflare R2 thất bại: " + e.getMessage(), e);
        }
    }

    public UploadPostResponse generatePresignedUrl(UploadUrlRequest request) throws Exception {
        if (!StorageConstants.ALLOWED_CONTENT_TYPES.contains(request.getContentType())) {
            throw new IllegalArgumentException("Loại file không hợp lệ");
        }

        // Chuẩn bị định dạng thời gian chuẩn AWS
        StorageUtils.AwsTimeInfo timeInfo = StorageUtils.prepareAwsTimeFormat();

        // Định nghĩa Object Key
        String objectKey = StorageUtils.generateObjectKey(request.getFolder(), request.getFileName(), StorageConstants.TEMP_FOLDER_PREFIX);

        // Lấy Access Key từ đối tượng properties
        String accessKey = properties.getAccessKey();

        // Tạo credential string dùng chung
        String credential = String.format("%s/%s/%s/%s/aws4_request",
                accessKey, timeInfo.dateStamp(), StorageConstants.REGION, StorageConstants.SERVICE);

        // Xây dựng Policy JSON
        String policyJson = StorageUtils.buildPolicyJson(
                timeInfo.expiration(), objectKey, request.getContentType(), credential, timeInfo.amzDate()
                , properties.getBucket(), StorageConstants.MAX_FILE_SIZE_BYTES, StorageConstants.ALGORITHM
        );

        // Encode Policy bằng Base64
        String base64Policy = StorageUtils.encodePolicyToBase64(policyJson);

        // Tính toán chữ ký Signature V4
        String signature = AwsSignatureUtils.calculateSignatureV4(
                base64Policy, timeInfo.dateStamp(), properties.getSecretKey(),
                StorageConstants.REGION, StorageConstants.SERVICE
        );

        // Đóng gói các trường để trả về cho Frontend
        return buildFinalResponse(
                objectKey, request.getContentType(), credential, timeInfo.amzDate(), base64Policy, signature
        );
    }

    private UploadPostResponse buildFinalResponse(
            String objectKey, String contentType, String credential,
            String amzDate, String base64Policy, String signature) {

        Map<String, String> formData = new HashMap<>();
        formData.put("key", objectKey);
        formData.put("Content-Type", contentType);
        formData.put("x-amz-credential", credential);
        formData.put("x-amz-algorithm", StorageConstants.ALGORITHM);
        formData.put("x-amz-date", amzDate);
        formData.put("Policy", base64Policy);
        formData.put("x-amz-signature", signature);

        // Lấy Account ID từ properties để build URL
        String endpointUrl = String.format("https://%s.r2.cloudflarestorage.com/%s",
                properties.getAccountId(), properties.getBucket());

        String baseUrl = properties.getPublicUrl();
        if (baseUrl != null && baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        String publicUrl = String.format("%s/%s", baseUrl, objectKey);

        return UploadPostResponse.builder()
                .uploadUrl(endpointUrl)
                .objectKey(objectKey)
                .formData(formData)
                .publicUrl(publicUrl)
                .build();
    }
}

