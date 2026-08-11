package com.pureblog.article.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleEvent implements Serializable {
    private String eventType;
    private Long articleId;
    private Long authorId;
    private String title;
    private String summary;
    private String content;
    private String htmlContent;
    private Long categoryId;
    private java.util.List<Long> tagIds;
    private LocalDateTime publishedAt;
}
