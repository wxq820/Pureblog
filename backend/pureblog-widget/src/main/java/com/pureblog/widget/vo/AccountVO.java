package com.pureblog.widget.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AccountVO {

    private Long id;
    private String name;
    private String icon;
    private String color;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
