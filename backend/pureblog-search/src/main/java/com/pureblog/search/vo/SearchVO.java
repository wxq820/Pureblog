package com.pureblog.search.vo;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchVO {
    private Long articleId;
    private String title;
    private String summary;
    private String authorName;
    private String authorAvatar;
    private Long authorId;
    private String categoryName;
    private List<String> tagNames;
    private Integer viewCount;
    private Integer likeCount;
    private String publishedAt;
    private List<String> highlights;
}
