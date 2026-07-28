package com.blog.be.storage.api.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateImagePrefixRequest {

    @NotEmpty(message = "Danh sách URL hình ảnh không được để trống")
    private List<String> imageUrls;
}
