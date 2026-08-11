package com.pureblog.search.document;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDocument {
    private String id;
    private Long articleId;
    private Long authorId;
    private String authorName;
    private String authorNickname;
    private String authorAvatar;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String summary;
    private String content;
    private List<String> tagNames;
    private List<String> tagSlugs;
    private List<Long> tagIds;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isFeatured;
    private String status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
}
