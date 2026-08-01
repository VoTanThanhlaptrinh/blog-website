package com.blog.backend.admin.application;

import com.blog.backend.admin.api.dto.AdminUserResponse;
import com.blog.backend.admin.api.dto.UpdateUserRoleRequest;
import com.blog.backend.admin.api.dto.UpdateUserStatusRequest;
import com.blog.backend.content.api.dto.PageResponse;
import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.identity.domain.enums.UserStatus;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {
    PageResponse<AdminUserResponse> getUsers(String role, UserStatus status, String keyword, Pageable pageable, User adminUser);
    AdminUserResponse updateUserStatus(Long userId, UpdateUserStatusRequest request, User adminUser);
    AdminUserResponse updateUserRole(Long userId, UpdateUserRoleRequest request, User adminUser);
    byte[] exportUsersCsv(String role, UserStatus status, String keyword, User adminUser);
}
