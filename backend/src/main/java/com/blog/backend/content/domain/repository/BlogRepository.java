package com.blog.backend.content.domain.repository;

import com.blog.backend.content.domain.entity.Blog;
import com.blog.backend.content.domain.enums.BlogStatus;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;

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

    long countByStatus(BlogStatus status);

    @Query("SELECT COUNT(DISTINCT b.user.id) FROM Blog b WHERE b.status = :status")
    long countDistinctUserByStatus(@Param("status") BlogStatus status);

    long countByCreatedDateBetween(LocalDateTime start, LocalDateTime end);

    long countByCreatedDateBetweenAndStatus(LocalDateTime start, LocalDateTime end, BlogStatus status);

    Page<Blog> findByStatusOrderByViewCountDesc(BlogStatus status, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT DISTINCT b.thumbnailUrl FROM Blog b WHERE b.user.id = :userId AND b.thumbnailUrl IS NOT NULL AND b.thumbnailUrl <> ''")
    java.util.List<String> findDistinctThumbnailUrlsByUserId(@Param("userId") Long userId);

    long countByUserIdAndStatusNot(Long userId, BlogStatus status);
}
