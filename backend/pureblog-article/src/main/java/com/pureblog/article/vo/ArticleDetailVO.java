package com.pureblog.article.vo;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDetailVO {
    private Long id;
    private String title;
    private String summary;
    private String coverUrl;
    private String content;
    private String htmlContent;
    private Integer wordCount;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer collectCount;
    private Boolean isFeatured;
    private Boolean isTop;
    private Boolean isLiked;
    private Boolean isCollected;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private AuthorVO author;
    private CategoryVO category;
    private List<TagVO> tags;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorVO {
        private Long userId;
        private String username;
        private String nickname;
        private String avatarUrl;
        private Integer followerCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryVO {
        private Long id;
        private String name;
        private String slug;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagVO {
        private Long id;
        private String name;
        private String slug;
    }
}
