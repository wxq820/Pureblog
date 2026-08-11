package com.pureblog.article.vo;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleListVO {
    private Long id;
    private String title;
    private String summary;
    private String coverUrl;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isFeatured;
    private Boolean isTop;
    private LocalDateTime publishedAt;
    private String authorName;
    private String authorAvatar;
    private Long authorId;
    private String categoryName;
    private List<String> tagNames;
}
