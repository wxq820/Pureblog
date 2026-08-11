package com.pureblog.article.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("pb_article_tag")
public class ArticleTagDO {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long articleId;
    private Long tagId;
    private java.time.LocalDateTime createdAt;
}
