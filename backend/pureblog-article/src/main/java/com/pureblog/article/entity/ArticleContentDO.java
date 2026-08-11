package com.pureblog.article.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("pb_article_content")
public class ArticleContentDO {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long articleId;
    private String content;
    private String htmlContent;
    private Integer wordCount;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
