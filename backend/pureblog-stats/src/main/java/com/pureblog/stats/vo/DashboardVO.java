package com.pureblog.stats.vo;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVO {
    private long totalArticles;
    private long totalUsers;
    private long totalComments;
    private long totalViews;
    private long todayViews;
    private long todayArticles;
    private long todayComments;
    private long pendingComments;
}
