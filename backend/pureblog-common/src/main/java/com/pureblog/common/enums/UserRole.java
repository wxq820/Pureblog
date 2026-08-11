package com.pureblog.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRole {
    USER(1, "普通用户"),
    AUTHOR(2, "作者"),
    ADMIN(3, "管理员"),
    SUPER_ADMIN(4, "超管");

    private final int code;
    private final String desc;

    public static UserRole of(Integer code) {
        if (code == null) return USER;
        for (UserRole role : values()) {
            if (role.code == code) return role;
        }
        return USER;
    }
}
