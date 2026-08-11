package com.pureblog.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pb_login_log")
public class LoginLogDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String username;

    private String ip;

    private String userAgent;

    private Integer status;

    private String msg;

    private LocalDateTime createdAt;
}
