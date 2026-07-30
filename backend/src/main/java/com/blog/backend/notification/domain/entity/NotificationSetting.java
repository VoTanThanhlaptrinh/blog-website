package com.blog.backend.notification.domain.entity;

import com.blog.backend.identity.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class NotificationSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    private boolean followers = true;

    @Builder.Default
    private boolean comments = true;

    @Builder.Default
    private boolean likes = false;

    @Builder.Default
    private boolean mentions = true;

    @Builder.Default
    private boolean newsletter = true;

    @Builder.Default
    private boolean features = true;

    @LastModifiedDate
    private LocalDateTime modifiedDate;
}
