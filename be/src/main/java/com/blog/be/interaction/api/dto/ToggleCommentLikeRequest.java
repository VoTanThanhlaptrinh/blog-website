package com.blog.be.interaction.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToggleCommentLikeRequest {
    @NotNull(message = "Comment ID không được để trống")
    private Long commentId;
}
