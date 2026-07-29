package com.blog.be.identity.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    private String phone;
    private java.time.LocalDate birthDate;
    private String avatarUrl;
    private String bio;
}
