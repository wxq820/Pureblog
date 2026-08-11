package com.pureblog.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ArticleStatus {
    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    OFFLINE(2, "已下架");

    private final int code;
    private final String desc;

    public static ArticleStatus of(Integer code) {
        if (code == null) return DRAFT;
        for (ArticleStatus status : values()) {
            if (status.code == code) return status;
        }
        return DRAFT;
    }
}
