package com.blog.backend.notification.application;

import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.notification.api.dto.NotificationSettingResponse;
import com.blog.backend.notification.api.dto.UpdateNotificationSettingRequest;
import com.blog.backend.notification.domain.entity.NotificationSetting;
import com.blog.backend.notification.domain.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationSettingService {

    private final NotificationSettingRepository notificationSettingRepository;

    @Transactional(readOnly = true)
    public NotificationSettingResponse getNotificationSetting(User user) {
        NotificationSetting setting = notificationSettingRepository.findByUser(user)
                .orElseGet(() -> NotificationSetting.builder()
                        .user(user)
                        .followers(true)
                        .comments(true)
                        .likes(false)
                        .mentions(true)
                        .newsletter(true)
                        .features(true)
                        .build());
        return mapToResponse(setting);
    }

    @Transactional
    public NotificationSettingResponse updateNotificationSetting(User user, UpdateNotificationSettingRequest request) {
        NotificationSetting setting = notificationSettingRepository.findByUser(user)
                .orElseGet(() -> NotificationSetting.builder().user(user).build());

        setting.setFollowers(request.isFollowers());
        setting.setComments(request.isComments());
        setting.setLikes(request.isLikes());
        setting.setMentions(request.isMentions());
        setting.setNewsletter(request.isNewsletter());
        setting.setFeatures(request.isFeatures());

        setting = notificationSettingRepository.save(setting);
        return mapToResponse(setting);
    }

    private NotificationSettingResponse mapToResponse(NotificationSetting setting) {
        return NotificationSettingResponse.builder()
                .followers(setting.isFollowers())
                .comments(setting.isComments())
                .likes(setting.isLikes())
                .mentions(setting.isMentions())
                .newsletter(setting.isNewsletter())
                .features(setting.isFeatures())
                .build();
    }
}
