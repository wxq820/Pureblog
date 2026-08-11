package com.pureblog.comment.vo;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleStatsVO {
    private Long articleId;
    private Integer likeCount;
    private Integer collectCount;
    private Integer commentCount;
    private Boolean isLiked;
    private Boolean isCollected;
}
