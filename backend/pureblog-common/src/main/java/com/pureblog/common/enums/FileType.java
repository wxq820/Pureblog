package com.pureblog.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileType {
    IMAGE(1, "图片"),
    VIDEO(2, "视频"),
    DOCUMENT(3, "文档"),
    OTHER(4, "其他");

    private final int code;
    private final String desc;
}
