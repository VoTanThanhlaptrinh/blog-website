package com.blog.be.identity.api.dto;

import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


public record RegisterRequest(
        @Email(message = "Khong phai email")
        @NotNull(message = "email bi rong")
        String email,
        @NotNull(message = "password bi rong")
        String password,
        @NotNull(message = "confirm password bi rong")
        String confirmPassword ){
        @AssertFalse(message = "password va confirm phai giong nhau")
        public boolean isConfirmPasswordValid(){
            return !password.equals(confirmPassword);
        }
}


