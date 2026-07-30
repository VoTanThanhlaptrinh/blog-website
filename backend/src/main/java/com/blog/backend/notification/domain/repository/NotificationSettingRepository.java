package com.blog.backend.notification.domain.repository;

import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.notification.domain.entity.NotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {
    Optional<NotificationSetting> findByUser(User user);
    Optional<NotificationSetting> findByUserId(Long userId);
}
