package com.pureblog.comment.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.pureblog.common.base.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pb_comment")
public class CommentDO extends BaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long articleId;
    private Long userId;
    private Long parentId;
    private Long replyToId;
    private Long replyToUid;
    private String content;
    private Integer likeCount;
    private Integer status;
}
