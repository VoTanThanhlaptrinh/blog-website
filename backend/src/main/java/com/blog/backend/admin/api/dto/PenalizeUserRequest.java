package com.blog.backend.admin.api.dto;

import com.blog.backend.admin.domain.enums.PenaltyAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PenalizeUserRequest {
    @NotNull(message = "Hình thức xử phạt không được để trống")
    private PenaltyAction action;

    @NotBlank(message = "Lý do xử phạt không được để trống")
    private String reason;
}
