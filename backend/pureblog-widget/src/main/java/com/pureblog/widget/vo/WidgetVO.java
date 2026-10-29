package com.pureblog.widget.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WidgetVO {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String icon;
    private String iconColor;
    private String path;
    private Integer sortOrder;
    private Integer status;
    private Integer clickCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
