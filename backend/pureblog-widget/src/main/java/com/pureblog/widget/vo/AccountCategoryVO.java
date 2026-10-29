package com.pureblog.widget.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AccountCategoryVO {

    private Long id;
    private String name;
    private Integer sortOrder;
    private Integer isCountedInTotal;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
