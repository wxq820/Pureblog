package com.pureblog.widget.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LedgerDictVO {

    private Long id;
    /** 1=平台 2=项目 */
    private Integer type;
    private String name;
    private String color;
    private String icon;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
}
