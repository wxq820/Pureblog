package com.pureblog.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pureblog.common.base.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pb_user")
public class UserDO extends BaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;

    private String email;

    private String passwordHash;

    private String nickname;

    private String avatarUrl;

    private String bio;

    private Integer role;

    private Integer status;

    private Integer followerCount;

    private Integer followingCount;

    private Integer articleCount;

    private java.time.LocalDateTime lastLoginAt;
}
