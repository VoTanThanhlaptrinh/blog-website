package com.blog.backend.storage.api;

import com.blog.backend.notification.api.ApiResponse;
import com.blog.backend.storage.api.dto.UploadPostResponse;
import com.blog.backend.storage.api.dto.UploadUrlRequest;
import com.blog.backend.storage.application.StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/file")
@RequiredArgsConstructor
public class FileController {
    private final StorageService storageService;

    @PostMapping()
    public ResponseEntity<ApiResponse<UploadPostResponse>> post(@RequestBody @Valid UploadUrlRequest uploadUrlRequest
            , BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null
                    , bindingResult.getAllErrors().get(0).getDefaultMessage(), 400));
        }

        try {
            UploadPostResponse response = storageService.generatePresignedUrl(uploadUrlRequest);
            return ResponseEntity.ok(new ApiResponse<>(response, "Upload URL generated successfully", 200));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, e.getMessage(), 400));
        }
    }
}
