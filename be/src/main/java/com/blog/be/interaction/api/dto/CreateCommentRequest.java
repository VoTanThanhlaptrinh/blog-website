package com.blog.be.interaction.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {
    @NotNull(message = "Blog ID không được để trống")
    private Long blogId;

    @NotBlank(message = "Nội dung bình luận không được để trống")
    private String content;

    private Long parentId;
}
