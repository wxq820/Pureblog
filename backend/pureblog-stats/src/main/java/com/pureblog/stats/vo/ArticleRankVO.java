package com.pureblog.stats.vo;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleRankVO {
    private Long articleId;
    private String title;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer hotScore;
}
