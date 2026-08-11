package com.pureblog.notification.vo;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationVO {
    private Long id;
    private Integer type;
    private String typeDesc;
    private String title;
    private String content;
    private Long relatedId;
    private Integer relatedType;
    private Boolean isRead;
    private String createdAt;
    private String relativeTime;
}
