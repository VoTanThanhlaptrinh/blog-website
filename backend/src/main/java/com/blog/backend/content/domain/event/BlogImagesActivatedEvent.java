package com.blog.backend.content.domain.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.Map;

/**
 * Published synchronously before a blog is saved so the storage module can confirm the images
 * that are still in file-service's temp/ area (they would be deleted otherwise).
 */
@Getter
public class BlogImagesActivatedEvent extends ApplicationEvent {
    /** Temp image URLs found in the blog's content and thumbnail. */
    private final List<String> imageUrls;

    /** Filled by the storage listener: temp URL -> permanent URL. */
    @Setter
    private Map<String, String> confirmedUrls = Map.of();

    public BlogImagesActivatedEvent(Object source, List<String> imageUrls) {
        super(source);
        this.imageUrls = imageUrls;
    }
}
