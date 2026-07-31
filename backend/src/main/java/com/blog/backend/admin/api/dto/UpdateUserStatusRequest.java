package com.blog.backend.admin.api.dto;

import com.blog.backend.identity.domain.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusRequest {
    private UserStatus status;
    private String reason;
}
