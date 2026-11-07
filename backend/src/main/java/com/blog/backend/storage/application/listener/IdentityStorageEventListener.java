package com.blog.backend.storage.application.listener;

import com.blog.backend.identity.domain.event.ProfileImageConfirmEvent;
import com.blog.backend.storage.application.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class IdentityStorageEventListener {

    private final StorageService storageService;

    /** Synchronous on purpose: a failed confirm aborts the profile update instead of saving a temp URL. */
    @EventListener
    public void handleProfileImageConfirmEvent(ProfileImageConfirmEvent event) {
        String permanentUrl = storageService.confirmTempUrls(List.of(event.getTempUrl())).get(event.getTempUrl());
        event.setPermanentUrl(permanentUrl);
    }
}
