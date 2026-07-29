package com.blog.be.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeStatsResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private long totalBlogs;
    private long totalAuthors;
    private long totalLikes;
    private long totalCategories;
}
