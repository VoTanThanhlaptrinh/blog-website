package com.blog.be.content.domain.repository;

import com.blog.be.content.domain.entity.Blog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long>, JpaSpecificationExecutor<Blog> {

    @Modifying
    @Query("UPDATE Blog b SET b.viewCount = b.viewCount + 1 WHERE b.id = :blogId")
    void incrementViewCount(@Param("blogId") Long blogId);

    @Modifying
    @Query("UPDATE Blog b SET b.shareCount = b.shareCount + 1 WHERE b.id = :blogId")
    void incrementShareCount(@Param("blogId") Long blogId);

    long countByStatus(com.blog.be.content.domain.enums.BlogStatus status);

    long countByCreatedDateBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);

    org.springframework.data.domain.Page<Blog> findByStatusOrderByViewCountDesc(com.blog.be.content.domain.enums.BlogStatus status, org.springframework.data.domain.Pageable pageable);
}
