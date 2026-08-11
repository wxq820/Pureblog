package com.pureblog.notification.event;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

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
    private LocalDateTime publishedAt;
}
