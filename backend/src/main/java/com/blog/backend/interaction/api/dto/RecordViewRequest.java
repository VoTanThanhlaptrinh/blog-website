package com.blog.backend.interaction.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordViewRequest {
    @NotNull(message = "Blog ID không được để trống")
    private Long blogId;
}
