package com.blog.backend.admin.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyGrowthResponse {
    private LocalDate date;
    private long newUsersCount;
    private long newBlogsCount;
}
