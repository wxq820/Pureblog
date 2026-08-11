package com.pureblog.stats.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("pb_article_stats")
public class ArticleStatsDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long articleId;
    private Integer pv;
    private Integer uv;
    private java.time.LocalDate date;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
