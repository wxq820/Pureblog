package com.pureblog.common.event;

public record CommentCreatedEvent(
        Long commentId,
        Long articleId,
        Long articleAuthorId,
        Long commentUserId,
        String content,
        Long parentId,
        Long replyToUid
) {
}
