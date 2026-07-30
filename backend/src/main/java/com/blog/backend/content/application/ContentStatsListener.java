package com.blog.backend.content.application;

import com.blog.backend.content.domain.enums.BlogStatus;
import com.blog.backend.content.domain.repository.BlogRepository;
import com.blog.backend.content.domain.repository.CategoryRepository;
import com.blog.backend.statistics.domain.event.GatherHomeStatsEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContentStatsListener {

    private final BlogRepository blogRepository;
    private final CategoryRepository categoryRepository;

    @EventListener
    public void onGatherHomeStats(GatherHomeStatsEvent event) {
        log.info("Received GatherHomeStatsEvent in content module, gathering stats...");
        event.addBlogs(blogRepository.countByStatus(BlogStatus.PUBLISHED));
        event.addAuthors(blogRepository.countDistinctUserByStatus(BlogStatus.PUBLISHED));
        event.addCategories(categoryRepository.count());
    }
}
