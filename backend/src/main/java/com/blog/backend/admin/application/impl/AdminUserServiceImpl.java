package com.blog.backend.admin.application.impl;

import com.blog.backend.admin.api.dto.AdminUserResponse;
import com.blog.backend.admin.api.dto.UpdateUserRoleRequest;
import com.blog.backend.admin.api.dto.UpdateUserStatusRequest;
import com.blog.backend.admin.application.AdminUserService;
import com.blog.backend.content.api.dto.PageResponse;
import com.blog.backend.content.domain.enums.BlogStatus;
import com.blog.backend.content.domain.exception.UnauthorizedBlogAccessException;
import com.blog.backend.content.domain.repository.BlogRepository;
import com.blog.backend.identity.domain.entity.Role;
import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.identity.domain.entity.UserRole;
import com.blog.backend.identity.domain.enums.UserRoleStatus;
import com.blog.backend.identity.domain.enums.UserStatus;
import com.blog.backend.identity.domain.repository.RoleRepository;
import com.blog.backend.identity.domain.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final BlogRepository blogRepository;
    private final RoleRepository roleRepository;

    private void validateAdmin(User adminUser) {
        if (adminUser == null || adminUser.getAuthorities() == null ||
                adminUser.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedBlogAccessException("Chỉ có Quản trị viên mới được phép thực hiện thao tác này");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> getUsers(String role, UserStatus status, String keyword, Pageable pageable, User adminUser) {
        validateAdmin(adminUser);

        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate p1 = cb.like(cb.lower(root.get("email")), pattern);
                Predicate p2 = cb.like(cb.lower(root.get("phone")), pattern);
                predicates.add(cb.or(p1, p2));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> pageResult = userRepository.findAll(spec, pageable);
        List<AdminUserResponse> content = pageResult.getContent().stream()
                .map(this::mapToAdminUserResponse)
                .collect(Collectors.toList());

        return PageResponse.<AdminUserResponse>builder()
                .content(content)
                .pageNumber(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    @Override
    @Transactional
    public AdminUserResponse updateUserStatus(Long userId, UpdateUserStatusRequest request, User adminUser) {
        validateAdmin(adminUser);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));

        user.setStatus(request.getStatus());
        if (request.getStatus() == UserStatus.ACTIVE) {
            user.setEnabled(true);
        } else if (request.getStatus() == UserStatus.BANNED) {
            user.setEnabled(false);
        }
        user = userRepository.save(user);

        return mapToAdminUserResponse(user);
    }

    @Override
    @Transactional
    public AdminUserResponse updateUserRole(Long userId, UpdateUserRoleRequest request, User adminUser) {
        validateAdmin(adminUser);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));

        String roleName = request.getRole();
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName.toUpperCase();
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy role: " + request.getRole()));

        if (user.getUserRoles() != null) {
            user.getUserRoles().clear();
        } else {
            user.setUserRoles(new ArrayList<>());
        }

        user.getUserRoles().add(UserRole.builder()
                .user(user)
                .role(role)
                .status(UserRoleStatus.ACTIVE)
                .build());

        user = userRepository.save(user);
        return mapToAdminUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportUsersCsv(String role, UserStatus status, String keyword, User adminUser) {
        validateAdmin(adminUser);
        List<User> users = userRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Email,Phone,Status,CreatedDate\n");
        for (User u : users) {
            sb.append(u.getId()).append(",")
              .append(u.getEmail() != null ? u.getEmail() : "").append(",")
              .append(u.getPhone() != null ? u.getPhone() : "").append(",")
              .append(u.getStatus() != null ? u.getStatus() : "").append(",")
              .append(u.getCreatedDate() != null ? u.getCreatedDate() : "").append("\n");
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private AdminUserResponse mapToAdminUserResponse(User user) {
        List<String> roles = user.getUserRoles() == null ? List.of() :
                user.getUserRoles().stream()
                        .filter(ur -> ur.getStatus() == UserRoleStatus.ACTIVE)
                        .map(ur -> ur.getRole().getName().replace("ROLE_", ""))
                        .collect(Collectors.toList());

        long postsCount = blogRepository.countByUserIdAndStatusNot(user.getId(), BlogStatus.DRAFT);

        return AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .bio(user.getBio())
                .avatarUrl(user.getAvatar() != null ? user.getAvatar().getUrl() : null)
                .status(user.getStatus())
                .roles(roles)
                .postsCount(postsCount)
                .createdDate(user.getCreatedDate())
                .build();
    }
}
