package com.pureblog.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatus {
    NORMAL(1, "正常"),
    DISABLED(2, "禁用");

    private final int code;
    private final String desc;
}
