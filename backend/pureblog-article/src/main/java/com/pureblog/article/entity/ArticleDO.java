package com.pureblog.article.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.pureblog.common.base.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pb_article")
public class ArticleDO extends BaseDO {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long authorId;
    private Long categoryId;
    private String title;
    private String summary;
    private String coverUrl;
    private Integer status;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer collectCount;
    private Integer isFeatured;
    private Integer isTop;
    private java.time.LocalDateTime publishedAt;
}
