package com.pureblog.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommentStatus {
    PENDING(0, "待审核"),
    APPROVED(1, "通过"),
    REJECTED(2, "拒绝");

    private final int code;
    private final String desc;

    public static CommentStatus of(Integer code) {
        if (code == null) return PENDING;
        for (CommentStatus status : values()) {
            if (status.code == code) return status;
        }
        return PENDING;
    }
}
