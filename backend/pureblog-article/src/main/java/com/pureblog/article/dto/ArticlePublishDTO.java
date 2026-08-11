package com.pureblog.article.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArticlePublishDTO {
    @NotNull(message = "文章ID不能为空")
    private Long id;
    
    private Integer isFeatured;
    private Integer isTop;
}
