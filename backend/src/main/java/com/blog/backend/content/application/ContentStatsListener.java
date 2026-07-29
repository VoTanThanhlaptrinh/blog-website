package com.blog.be.content.application;

import com.blog.be.content.domain.enums.BlogStatus;
import com.blog.be.content.domain.repository.BlogRepository;
import com.blog.be.content.domain.repository.CategoryRepository;
import com.blog.be.statistics.domain.event.GatherHomeStatsEvent;
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
