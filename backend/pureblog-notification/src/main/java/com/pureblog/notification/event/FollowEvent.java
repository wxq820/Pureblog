package com.pureblog.notification.event;

import lombok.*;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowEvent implements Serializable {
    private String eventType;
    private Long followerId;
    private Long followingId;
}
