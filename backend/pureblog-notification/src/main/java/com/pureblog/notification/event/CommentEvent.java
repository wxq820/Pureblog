package com.pureblog.notification.event;

import lombok.*;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentEvent implements Serializable {
    private String eventType;
    private Long commentId;
    private Long articleId;
    private Long articleAuthorId;
    private Long commentUserId;
    private String content;
    private Long parentId;
    private Long replyToUid;
}
