package com.blog.backend.identity.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public record AccountLoginRequest(
        @Email(message = "Khong phai email")
        @NotNull(message = "email bi rong") String email
        ,@NotNull(message = "password bi rong") String password) {

}
