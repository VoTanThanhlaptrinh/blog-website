package com.blog.backend.admin.application.impl;

import com.blog.backend.admin.application.AdminSystemSettingService;
import com.blog.backend.admin.domain.entity.SystemSetting;
import com.blog.backend.admin.domain.repository.SystemSettingRepository;
import com.blog.backend.content.domain.exception.UnauthorizedBlogAccessException;
import com.blog.backend.identity.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminSystemSettingServiceImpl implements AdminSystemSettingService {

    private final SystemSettingRepository systemSettingRepository;

    private void validateAdmin(User adminUser) {
        if (adminUser == null || adminUser.getAuthorities() == null ||
                adminUser.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedBlogAccessException("Chỉ có Quản trị viên mới được phép thực hiện thao tác này");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getSystemSettings(User adminUser) {
        validateAdmin(adminUser);
        List<SystemSetting> settingsList = systemSettingRepository.findAll();
        Map<String, String> resultMap = new HashMap<>();

        // Default values
        resultMap.put("siteName", "B-BlogHub");
        resultMap.put("siteDescription", "Nền tảng chia sẻ bài viết kỹ thuật & công nghệ");
        resultMap.put("maintenanceMode", "false");
        resultMap.put("maxUploadSizeMb", "10");

        for (SystemSetting setting : settingsList) {
            resultMap.put(setting.getKey(), setting.getValue());
        }

        return resultMap;
    }

    @Override
    @Transactional
    public Map<String, String> updateSystemSettings(Map<String, String> settings, User adminUser) {
        validateAdmin(adminUser);
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            SystemSetting setting = systemSettingRepository.findByKey(entry.getKey())
                    .orElse(SystemSetting.builder()
                            .key(entry.getKey())
                            .build());
            setting.setValue(entry.getValue());
            systemSettingRepository.save(setting);
        }
        return getSystemSettings(adminUser);
    }
}
