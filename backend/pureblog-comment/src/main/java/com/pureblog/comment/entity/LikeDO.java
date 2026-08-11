package com.pureblog.comment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("pb_like")
public class LikeDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private Long targetId;
    private Integer targetType;
    private java.time.LocalDateTime createdAt;
}
