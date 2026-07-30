package com.blog.backend.content.domain.event;

import org.springframework.context.ApplicationEvent;
import java.util.List;

public class BlogImagesActivatedEvent extends ApplicationEvent {
    private final List<String> imageUrls;
    private final String sourcePrefix;
    private final String targetPrefix;

    public BlogImagesActivatedEvent(Object source, List<String> imageUrls, String sourcePrefix, String targetPrefix) {
        super(source);
        this.imageUrls = imageUrls;
        this.sourcePrefix = sourcePrefix;
        this.targetPrefix = targetPrefix;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public String getSourcePrefix() {
        return sourcePrefix;
    }

    public String getTargetPrefix() {
        return targetPrefix;
    }
}
