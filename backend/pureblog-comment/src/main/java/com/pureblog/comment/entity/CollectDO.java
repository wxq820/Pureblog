package com.pureblog.comment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("pb_collect")
public class CollectDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private Long articleId;
    private java.time.LocalDateTime createdAt;
}
