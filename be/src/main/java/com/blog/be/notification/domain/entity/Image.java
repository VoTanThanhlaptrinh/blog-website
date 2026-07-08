package com.blog.be.notification.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.blog.be.notification.domain.enums.ImageStatus;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Getter
public class Image {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String url;
    private String description;
    private String contentType;
    @Enumerated(EnumType.STRING)
    private ImageStatus status;
    @CreatedDate
    private LocalDateTime createdDate;
    @LastModifiedDate
    private LocalDateTime modifiedDate;
}
