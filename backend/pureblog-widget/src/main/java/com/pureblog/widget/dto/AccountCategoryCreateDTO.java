package com.pureblog.widget.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AccountCategoryCreateDTO {

    @NotBlank(message = "分类名不能为空")
    @Size(max = 32)
    private String name;

    private Integer sortOrder;
    /** 0/1, 默认 1. 0 表示不计入"目前完整身家" */
    private Integer isCountedInTotal;
    private Integer status;
}
