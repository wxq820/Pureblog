package com.pureblog.notification.event;

import lombok.*;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsEvent implements Serializable {
    private String eventType;
    private Long articleId;
    private Long articleAuthorId;
    private Long userId;
}
