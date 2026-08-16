package com.pureblog.common.event;

public record StatsLikeArticleEvent(
        Long articleId,
        Long articleAuthorId,
        Long userId
) {
}
