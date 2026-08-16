package com.pureblog.common.event;

public record StatsPvEvent(
        Long articleId,
        String ip
) {
}
