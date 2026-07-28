package com.blog.be.identity.domain.repository;

import com.blog.be.identity.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserById(Long id);

    Optional<User> findUserByEmail(String email);

    long countByCreatedDateBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
