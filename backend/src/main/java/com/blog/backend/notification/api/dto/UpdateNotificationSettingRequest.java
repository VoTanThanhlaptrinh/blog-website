package com.blog.backend.notification.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNotificationSettingRequest {
    private boolean followers;
    private boolean comments;
    private boolean likes;
    private boolean mentions;
    private boolean newsletter;
    private boolean features;
}
