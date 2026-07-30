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

    @EventListener
    public void handleBlogImagesActivatedEvent(BlogImagesActivatedEvent event) {
        log.info("Received BlogImagesActivatedEvent for {} image URLs", event.getImageUrls() != null ? event.getImageUrls().size() : 0);
        try {
            storageService.updateImagePrefixes(event.getImageUrls(), event.getSourcePrefix(), event.getTargetPrefix());
            log.info("Successfully processed BlogImagesActivatedEvent");
        } catch (Exception e) {
            log.error("Failed to process BlogImagesActivatedEvent: {}", e.getMessage(), e);
        }
    }
}
