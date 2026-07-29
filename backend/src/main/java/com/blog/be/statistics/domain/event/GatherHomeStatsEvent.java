package com.blog.be.statistics.domain.event;

import lombok.Getter;

@Getter
public class GatherHomeStatsEvent {
    private long totalBlogs = 0;
    private long totalAuthors = 0;
    private long totalLikes = 0;
    private long totalCategories = 0;

    public void addBlogs(long count) {
        this.totalBlogs += count;
    }

    public void addAuthors(long count) {
        this.totalAuthors += count;
    }

    public void addLikes(long count) {
        this.totalLikes += count;
    }

    public void addCategories(long count) {
        this.totalCategories += count;
    }
}
