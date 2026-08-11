package com.pureblog.admin.dto;

import lombok.Data;

@Data
public class ArticleAdminQueryDTO {
    private Long authorId;
    private Integer status;
    private String keyword;
    private Integer page = 1;
    private Integer size = 10;
}
