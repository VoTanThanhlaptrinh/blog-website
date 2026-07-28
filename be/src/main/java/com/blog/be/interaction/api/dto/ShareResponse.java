package com.blog.be.interaction.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareResponse {
    private Long id;
    private Long blogId;
    private String provider;
    private int totalShares;
    private LocalDateTime createdDate;
}
