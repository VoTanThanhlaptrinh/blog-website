package com.blog.be.interaction.domain.repository;

import com.blog.be.interaction.domain.entity.View;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ViewRepository extends JpaRepository<View, Long> {

    boolean existsByBlogIdAndUserIdAndCreatedDateAfter(Long blogId, Long userId, LocalDateTime after);

    boolean existsByBlogIdAndIpAddressAndCreatedDateAfter(Long blogId, String ipAddress, LocalDateTime after);
}
