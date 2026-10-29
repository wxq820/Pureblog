package com.pureblog.article.mapper;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 搜索结果的单行投影, 由 SearchMapper.xml 直接映射。
 *
 * <p>与 {@link com.pureblog.article.vo.SearchVO} 的区别:
 * tagNames 为逗号分隔的原始字符串 (GROUP_CONCAT 结果), content 用于生成正文高亮片段。
 */
@Data
public class SearchRow {
    private Long articleId;
    private String title;
    private String summary;
    private String content;
    private Long authorId;
    private String authorName;
    private String authorAvatar;
    private String categoryName;
    private String tagNames;
    private Integer viewCount;
    private Integer likeCount;
    private LocalDateTime publishedAt;
}
