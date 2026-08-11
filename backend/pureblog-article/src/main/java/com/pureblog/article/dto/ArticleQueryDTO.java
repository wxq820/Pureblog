package com.pureblog.article.dto;

import lombok.Data;

@Data
public class ArticleQueryDTO {
    private Long categoryId;
    private Long tagId;
    private Long authorId;
    private Integer status;
    private String keyword;
    private String sortBy = "publishedAt";
    private String sortOrder = "desc";
    private Integer page = 1;
    private Integer size = 10;
}
