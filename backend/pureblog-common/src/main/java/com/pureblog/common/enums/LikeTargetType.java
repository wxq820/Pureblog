package com.pureblog.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LikeTargetType {
    ARTICLE(1, "文章"),
    COMMENT(2, "评论");

    private final int code;
    private final String desc;
}
