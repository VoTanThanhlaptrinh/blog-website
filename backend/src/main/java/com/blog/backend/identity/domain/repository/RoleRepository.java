package com.blog.backend.identity.domain.repository;

import com.blog.backend.identity.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    java.util.Optional<Role> findByName(String name);
}
