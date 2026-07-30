package com.blog.backend.content.domain.entity;

import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.interaction.domain.entity.Comment;
import com.blog.backend.interaction.domain.entity.Like;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.blog.backend.content.domain.enums.BlogStatus;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "blogs")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Blog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private String content;
    @Enumerated(EnumType.STRING)
    private BlogStatus status;
    private String rejectionReason;
    @Builder.Default
    private int viewCount = 0;
    @Builder.Default
    private int shareCount = 0;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "blog", fetch = FetchType.LAZY)
    private List<Comment> comments;
    @OneToMany(mappedBy = "blog", fetch = FetchType.LAZY)
    private List<Like> likes;
    @CreatedDate
    private LocalDateTime createdDate;
    @LastModifiedDate
    private LocalDateTime modifiedDate;

}
