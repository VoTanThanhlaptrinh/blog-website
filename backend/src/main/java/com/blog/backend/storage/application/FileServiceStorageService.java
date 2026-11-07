package com.blog.backend.storage.application;

import com.blog.backend.storage.api.dto.UploadPostResponse;
import com.blog.backend.storage.api.dto.UploadUrlRequest;
import com.blog.backend.storage.domain.exception.FileServiceException;
import com.blog.backend.storage.domain.exception.StorageErrorCode;
import com.blog.backend.storage.infrastructure.fileservice.FileServiceApi;
import com.blog.backend.storage.infrastructure.fileservice.dto.ConfirmBatchRequest;
import com.blog.backend.storage.infrastructure.fileservice.dto.ConfirmResult;
import com.blog.backend.storage.infrastructure.fileservice.dto.PresignRequest;
import com.blog.backend.storage.infrastructure.fileservice.dto.PresignResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceStorageService implements StorageService {

    /** Temp keys file-service generates for the blog: temp/public/blog/{context}/{YYYY-MM}/{uuid}[.ext] */
    private static final Pattern BLOG_TEMP_KEY = Pattern.compile(
            "temp/public/blog/[a-z0-9-]+/\\d{4}-\\d{2}/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}(?:\\.[A-Za-z0-9]+)?");

    /** Frontend folder -> file-service context (see file-service/src/config/app-policies.ts). */
    private static final Map<String, String> FOLDER_CONTEXTS = Map.of(
            "blog/temp", "post-image",
            "blog/thumbnails", "post-image",
            "profile/avatar", "avatar");

    private final FileServiceApi fileServiceApi;

    @Override
    public UploadPostResponse createUploadUrl(UploadUrlRequest request) {
        String context = FOLDER_CONTEXTS.get(request.getFolder());
        if (context == null) {
            throw new FileServiceException(StorageErrorCode.INVALID_UPLOAD,
                    "Thư mục upload không hợp lệ: " + request.getFolder());
        }

        PresignRequest presignRequest = new PresignRequest(context,
                new PresignRequest.FileItem(request.getFileName(), request.getContentType(), request.getFileSize()));
        PresignResult result = call(() -> fileServiceApi.presign(presignRequest).data());

        return UploadPostResponse.builder()
                .uploadUrl(result.uploadUrl())
                .objectKey(result.tempKey())
                .formData(Map.of())
                .publicUrl(result.previewUrl())
                .build();
    }

    @Override
    public Map<String, String> confirmTempUrls(Collection<String> urls) {
        Map<String, String> tempKeyByUrl = new LinkedHashMap<>();
        for (String url : urls) {
            if (url == null) {
                continue;
            }
            Matcher matcher = BLOG_TEMP_KEY.matcher(url);
            if (matcher.find()) {
                tempKeyByUrl.put(url, matcher.group());
            }
        }
        if (tempKeyByUrl.isEmpty()) {
            return Map.of();
        }

        List<String> tempKeys = new ArrayList<>(new LinkedHashSet<>(tempKeyByUrl.values()));
        List<ConfirmResult> results = call(() -> fileServiceApi.confirmBatch(new ConfirmBatchRequest(tempKeys)).data());

        // confirm-batch answers in request order
        Map<String, String> permanentUrlByTempKey = new LinkedHashMap<>();
        for (int i = 0; i < tempKeys.size(); i++) {
            permanentUrlByTempKey.put(tempKeys.get(i), results.get(i).url());
        }

        Map<String, String> confirmed = new LinkedHashMap<>();
        tempKeyByUrl.forEach((url, tempKey) -> confirmed.put(url, permanentUrlByTempKey.get(tempKey)));
        return confirmed;
    }

    /** Turns "file-service is down / unreachable" into a 503 instead of a generic 500. */
    private <T> T call(Supplier<T> request) {
        try {
            return request.get();
        } catch (ResourceAccessException e) {
            log.error("Cannot reach file-service: {}", e.getMessage());
            throw new FileServiceException(StorageErrorCode.FILE_SERVICE_UNAVAILABLE);
        }
    }
}
