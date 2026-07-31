package com.blog.backend.identity.api.dto;

import com.blog.backend.identity.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String email;
    private String phone;
    private String birthDate;
    private String avatarUrl;
    private String bio;
    private List<String> roles;
}
