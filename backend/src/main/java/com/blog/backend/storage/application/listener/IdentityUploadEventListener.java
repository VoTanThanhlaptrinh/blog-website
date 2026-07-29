package com.blog.backend.storage.application.listener;

import com.blog.backend.identity.domain.event.ProfileImageConfirmEvent;
import com.blog.backend.identity.domain.event.ProfileImageUploadEvent;
import com.blog.backend.storage.api.dto.UploadPostResponse;
import com.blog.backend.storage.api.dto.UploadUrlRequest;
import com.blog.backend.storage.application.StorageService;
import com.blog.backend.storage.application.util.AwsSignatureUtils;
import com.blog.backend.storage.application.util.StorageUtils;
import com.blog.backend.storage.domain.config.StorageProperties;
import com.blog.backend.storage.domain.entity.Image;
import com.blog.backend.storage.domain.repository.ImageRepository;
import com.blog.backend.storage.infrastructure.cloudflare.config.CloudflareR2Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdentityUploadEventListener {

    private final StorageProperties storageProperties;
    private final CloudflareR2Properties cloudflareR2Properties;
    private final StorageService storageService;
    private final ImageRepository imageRepository;

    @EventListener
    public void handleProfileImageUploadEvent(ProfileImageUploadEvent event) {
        try {
            UploadUrlRequest request = event.getRequest();
            StorageProperties.ModuleStorageProperties identityConfig = storageProperties.getIdentity();

            if (!identityConfig.getAllowedContentTypes().contains(request.getContentType())) {
                throw new IllegalArgumentException("Loại file không hợp lệ cho identity");
            }

            StorageUtils.AwsTimeInfo timeInfo = StorageUtils.prepareAwsTimeFormat();
            String objectKey = StorageUtils.generateObjectKey(request.getFolder(), request.getFileName(), identityConfig.getTempFolderPrefix());

            String accessKey = cloudflareR2Properties.getAccessKey();
            String credential = String.format("%s/%s/%s/%s/aws4_request",
                    accessKey, timeInfo.dateStamp(), identityConfig.getRegion(), identityConfig.getService());

            String policyJson = StorageUtils.buildPolicyJson(
                    timeInfo.expiration(), objectKey, request.getContentType(), credential, timeInfo.amzDate(),
                    cloudflareR2Properties.getBucket(), identityConfig.getMaxFileSizeBytes(), identityConfig.getAlgorithm()
            );

            String base64Policy = StorageUtils.encodePolicyToBase64(policyJson);

            String signature = AwsSignatureUtils.calculateSignatureV4(
                    base64Policy, timeInfo.dateStamp(), cloudflareR2Properties.getSecretKey(),
                    identityConfig.getRegion(), identityConfig.getService()
            );

            Map<String, String> formData = new HashMap<>();
            formData.put("key", objectKey);
            formData.put("Content-Type", request.getContentType());
            formData.put("x-amz-credential", credential);
            formData.put("x-amz-algorithm", identityConfig.getAlgorithm());
            formData.put("x-amz-date", timeInfo.amzDate());
            formData.put("Policy", base64Policy);
            formData.put("x-amz-signature", signature);

            String endpointUrl = String.format("https://%s.r2.cloudflarestorage.com/%s",
                    cloudflareR2Properties.getAccountId(), cloudflareR2Properties.getBucket());

            String baseUrl = cloudflareR2Properties.getPublicUrl();
            if (baseUrl != null && baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            String publicUrl = String.format("%s/%s", baseUrl, objectKey);

            UploadPostResponse response = UploadPostResponse.builder()
                    .uploadUrl(endpointUrl)
                    .objectKey(objectKey)
                    .formData(formData)
                    .publicUrl(publicUrl)
                    .build();


            event.setResponse(response);
        } catch (Exception e) {
            log.error("Error generating presigned url for identity upload", e);
            throw new RuntimeException("Lỗi cấp url upload ảnh profile", e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleProfileImageConfirmEvent(ProfileImageConfirmEvent event) {
        String tempUrl = event.getTempUrl();
        if (tempUrl == null || tempUrl.isBlank() || event.getImageId() == null) {
            return;
        }

        String tempPrefix = storageProperties.getIdentity().getTempFolderPrefix();
        int index = tempUrl.indexOf(tempPrefix);
        
        if (index >= 0) {
            try {
                // Lấy phần object key thực sự từ URL (ví dụ: identity/temp/avatar/123.jpg)
                String tempKey = tempUrl.substring(index);
                String permanentUrl = storageService.confirmAndActivateFile(tempKey, tempPrefix);
                
                // Cập nhật lại Image entity trong database ở transaction mới
                Image image = imageRepository.findById(event.getImageId()).orElse(null);
                if (image != null) {
                    image.setUrl(permanentUrl);
                    imageRepository.save(image);
                }
            } catch (Exception e) {
                log.error("Error confirming profile image", e);
            }
        }
    }
}
