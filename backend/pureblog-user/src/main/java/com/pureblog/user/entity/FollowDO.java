package com.pureblog.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pureblog.common.base.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pb_follow")
public class FollowDO extends BaseDO {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long followerId;
    
    private Long followingId;
}
