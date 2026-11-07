package com.blog.backend.content.application;

import com.blog.backend.admin.domain.event.AdminGetBlogEvent;
import com.blog.backend.content.api.dto.BlogResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBlogEventListener {

    private final BlogService blogService;

    @EventListener
    public void onAdminGetBlog(AdminGetBlogEvent event) {
        log.info("Handling AdminGetBlogEvent for blogId: {}", event.getBlogId());
        BlogResponse blogResponse = blogService.getBlogById(event.getBlogId(), event.getAdminUser());
        event.setResult(blogResponse);
    }
}
