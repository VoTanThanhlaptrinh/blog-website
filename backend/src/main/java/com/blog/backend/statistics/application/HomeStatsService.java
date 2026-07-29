package com.blog.be.statistics.application;

import com.blog.be.statistics.domain.event.GatherHomeStatsEvent;
import com.blog.be.statistics.dto.HomeStatsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class HomeStatsService {

    private final ApplicationEventPublisher eventPublisher;

    @Cacheable(value = "homeStats", key = "'home'")
    public HomeStatsResponse getHomeStats() {
        log.info("Cache miss! Calculating Home Stats via Events...");

        GatherHomeStatsEvent event = new GatherHomeStatsEvent();
        eventPublisher.publishEvent(event);

        return HomeStatsResponse.builder()
                .totalBlogs(event.getTotalBlogs())
                .totalAuthors(event.getTotalAuthors())
                .totalLikes(event.getTotalLikes())
                .totalCategories(event.getTotalCategories())
                .build();
    }
}
