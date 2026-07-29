package com.blog.backend.interaction.application;

import com.blog.backend.interaction.domain.repository.LikeRepository;
import com.blog.backend.statistics.domain.event.GatherHomeStatsEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InteractionStatsListener {

    private final LikeRepository likeRepository;

    @EventListener
    public void onGatherHomeStats(GatherHomeStatsEvent event) {
        log.info("Received GatherHomeStatsEvent in interaction module, gathering stats...");
        event.addLikes(likeRepository.countByLikedTrue());
    }
}
