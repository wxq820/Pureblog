package com.pureblog.article.dto;

import lombok.Data;

@Data
public class ArticleQueryDTO {
    private Long categoryId;
    private Long tagId;
    private Long authorId;
    private Long treeId;
    private Long treeNodeId;
    private Integer status;
    /**
     * 关键词, 匹配范围: 标题 OR 摘要 OR 正文 OR 标签名.
     * 与 {@code GET /api/search} 的匹配范围保持一致.
     */
    private String keyword;
    private String sortBy = "publishedAt";
    private String sortOrder = "desc";
    private Integer page = 1;
    private Integer size = 10;
}
