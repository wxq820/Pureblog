package com.pureblog.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {
    COMMENT(1, "评论通知"),
    REPLY(2, "回复通知"),
    FOLLOW(3, "关注通知"),
    LIKE(4, "点赞通知"),
    COLLECT(5, "收藏通知"),
    SYSTEM(6, "系统通知");

    private final int code;
    private final String desc;
}
