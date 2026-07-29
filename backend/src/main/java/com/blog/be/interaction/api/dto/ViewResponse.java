package com.blog.be.interaction.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewResponse {
    private Long blogId;
    private boolean recorded;
    private int totalViews;
}
