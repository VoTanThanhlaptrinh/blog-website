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
public class ToggleLikeRequest {
    @NotNull(message = "Blog ID không được để trống")
    private Long blogId;
}
