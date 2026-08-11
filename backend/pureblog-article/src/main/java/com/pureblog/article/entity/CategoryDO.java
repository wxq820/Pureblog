package com.pureblog.article.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.pureblog.common.base.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pb_category")
public class CategoryDO extends BaseDO {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private String name;
    private String slug;
    private String description;
    private Integer sortOrder;
    private Integer articleCount;
}
