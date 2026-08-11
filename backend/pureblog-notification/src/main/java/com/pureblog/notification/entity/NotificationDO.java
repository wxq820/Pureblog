package com.pureblog.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("pb_notification")
public class NotificationDO {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long userId;
    private Integer type;
    private String title;
    private String content;
    private Long relatedId;
    private Integer relatedType;
    private Integer isRead;
    private java.time.LocalDateTime createdAt;
}
