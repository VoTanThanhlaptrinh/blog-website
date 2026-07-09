package com.blog.be.storage.application;

import com.blog.be.storage.api.dto.UploadPostResponse;
import com.blog.be.storage.api.dto.UploadUrlRequest;
import com.blog.be.storage.api.dto.UploadUrlResponse;
import com.blog.be.storage.domain.port.FileStoragePort;
import com.blog.be.storage.infrastructure.cloudflare.config.CloudflareR2Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final FileStoragePort fileStoragePort;
    private final CloudflareR2Properties properties;

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final String REGION = "auto";
    private static final String SERVICE = "s3";
    private static final String ALGORITHM = "AWS4-HMAC-SHA256";
    private static final long MAX_FILE_SIZE_BYTES = 5242880L;

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

    @Override
    public UploadUrlResponse generateUploadUrl(UploadUrlRequest uploadUrlRequest) {
        return null;
    }

    private String extractKey(String fileUrl) {
        if (fileUrl != null && fileUrl.contains("/")) {
            return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        }
        return fileUrl;
    }
    private record AwsTimeInfo(String amzDate, String dateStamp, String expiration) {}
    
    public UploadPostResponse generateStrictUploadPolicy(UploadUrlRequest request) throws Exception {
        if (!ALLOWED_CONTENT_TYPES.contains(request.getContentType())) {
            throw new IllegalArgumentException("Loại file không hợp lệ");
        }

        // Chuẩn bị định dạng thời gian chuẩn AWS
        AwsTimeInfo timeInfo = prepareAwsTimeFormat();

        // Định nghĩa Object Key
        String objectKey = generateObjectKey(request.getFolder(), request.getFileName());

        // Lấy Access Key từ đối tượng properties
        String accessKey = properties.getAccessKey();

        // Tạo credential string dùng chung
        String credential = String.format("%s/%s/%s/%s/aws4_request",
                accessKey, timeInfo.dateStamp(), REGION, SERVICE);

        // Xây dựng Policy JSON
        String policyJson = buildPolicyJson(
                timeInfo.expiration(), objectKey, request.getContentType(), credential, timeInfo.amzDate()
                , properties.getBucket()
        );

        // Encode Policy bằng Base64
        String base64Policy = encodePolicyToBase64(policyJson);

        // Tính toán chữ ký Signature V4
        String signature = calculateSignatureV4(base64Policy, timeInfo.dateStamp(), properties.getSecretKey());

        // Đóng gói các trường để trả về cho Frontend
        return buildFinalResponse(
                objectKey, request.getContentType(), credential, timeInfo.amzDate(), base64Policy, signature
        );
    }

    private AwsTimeInfo prepareAwsTimeFormat() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        String amzDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
        String dateStamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String expiration = now.plusMinutes(15).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));

        return new AwsTimeInfo(amzDate, dateStamp, expiration);
    }

    private String generateObjectKey(String folder, String fileName) {
        String safeFolder = (folder != null && !folder.isBlank()) ? folder.toLowerCase() : "others";
        String safeFileName = fileName.replaceAll("[^a-zA-Z0-9.-]", "_");

        return String.format("temp/%s/%s-%s", safeFolder, UUID.randomUUID(), safeFileName);
    }

    private String buildPolicyJson(String expiration, String objectKey, String contentType, String credential, String amzDate, String bucketName) {
        return String.format(
                "{\n" +
                        "  \"expiration\": \"%s\",\n" +
                        "  \"conditions\": [\n" +
                        "    {\"bucket\": \"%s\"},\n" +
                        "    {\"key\": \"%s\"},\n" +
                        "    {\"Content-Type\": \"%s\"},\n" +
                        "    [\"content-length-range\", 0, %d],\n" +
                        "    {\"x-amz-credential\": \"%s\"},\n" +
                        "    {\"x-amz-algorithm\": \"%s\"},\n" +
                        "    {\"x-amz-date\": \"%s\"}\n" +
                        "  ]\n" +
                        "}",
                expiration, bucketName, objectKey, contentType, MAX_FILE_SIZE_BYTES, credential, ALGORITHM, amzDate
        );
    }

    private String encodePolicyToBase64(String policyJson) {
        return Base64.getEncoder().encodeToString(policyJson.getBytes(StandardCharsets.UTF_8));
    }

    private String calculateSignatureV4(String base64Policy, String dateStamp, String secretKey) throws Exception {
        byte[] signatureKey = getSignatureKey(secretKey, dateStamp);
        byte[] signatureBytes = hmacSHA256(base64Policy, signatureKey);

        StringBuilder signatureHex = new StringBuilder();
        for (byte b : signatureBytes) {
            signatureHex.append(String.format("%02x", b));
        }
        return signatureHex.toString();
    }

    private UploadPostResponse buildFinalResponse(
            String objectKey, String contentType, String credential,
            String amzDate, String base64Policy, String signature) {

        Map<String, String> formData = new HashMap<>();
        formData.put("key", objectKey);
        formData.put("Content-Type", contentType);
        formData.put("x-amz-credential", credential);
        formData.put("x-amz-algorithm", ALGORITHM);
        formData.put("x-amz-date", amzDate);
        formData.put("Policy", base64Policy);
        formData.put("x-amz-signature", signature);

        // Lấy Account ID từ properties để build URL
        String endpointUrl = String.format("https://%s.r2.cloudflarestorage.com/%s",
                properties.getAccountId(), properties.getBucket());

        return UploadPostResponse.builder()
                .uploadUrl(endpointUrl)
                .objectKey(objectKey)
                .formData(formData)
                .build();
    }

    // =======================================================================
    // CÁC HÀM TIỆN ÍCH MÃ HÓA (UTILITIES)
    // =======================================================================

    private byte[] hmacSHA256(String data, byte[] key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] getSignatureKey(String key, String dateStamp) throws Exception {
        byte[] kSecret = ("AWS4" + key).getBytes(StandardCharsets.UTF_8);
        byte[] kDate = hmacSHA256(dateStamp, kSecret);
        byte[] kRegion = hmacSHA256(REGION, kDate);
        byte[] kService = hmacSHA256(SERVICE, kRegion);
        return hmacSHA256("aws4_request", kService);
    }
}
