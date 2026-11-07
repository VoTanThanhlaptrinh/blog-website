package com.blog.backend.admin.application;

import com.blog.backend.identity.domain.entity.User;
import java.util.Map;

public interface AdminSystemSettingService {
    Map<String, String> getSystemSettings(User adminUser);
    Map<String, String> updateSystemSettings(Map<String, String> settings, User adminUser);
}
