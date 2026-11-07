package com.blog.backend.storage.application.listener;

import com.blog.backend.content.domain.event.BlogImagesActivatedEvent;
import com.blog.backend.storage.application.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentStorageEventListener {

    private final StorageService storageService;

    /** Synchronous on purpose: errors propagate so the blog is not saved with links that will expire. */
    @EventListener
    public void handleBlogImagesActivatedEvent(BlogImagesActivatedEvent event) {
        event.setConfirmedUrls(storageService.confirmTempUrls(event.getImageUrls()));
        log.info("Confirmed {} blog image(s) with file-service", event.getConfirmedUrls().size());
    }
}
