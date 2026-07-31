package com.blog.backend.identity.domain.repository;

import com.blog.backend.identity.domain.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserById(Long id);

    @EntityGraph(attributePaths = { "userRoles", "userRoles.role", "avatar" })
    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findUserByIdWithRoles(@org.springframework.data.repository.query.Param("id") Long id);

    @EntityGraph(attributePaths = { "userRoles", "userRoles.role", "avatar" })
    Optional<User> findUserByEmail(String email);

    long countByCreatedDateBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
