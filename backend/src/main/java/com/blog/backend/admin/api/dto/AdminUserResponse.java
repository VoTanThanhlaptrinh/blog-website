package com.blog.backend.admin.api.dto;

import com.blog.backend.identity.domain.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {
    private Long id;
    private String email;
    private String phone;
    private String bio;
    private String avatarUrl;
    private UserStatus status;
    private List<String> roles;
    private long postsCount;
    private long viewsCount;
    private LocalDateTime createdDate;
}
