package com.pureblog.widget.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AccountCreateDTO {

    @NotBlank(message = "账户名不能为空")
    @Size(max = 64)
    private String name;

    @Size(max = 32)
    private String icon;

    @Size(max = 16)
    private String color;

    private Integer sortOrder;
    private Integer status;
}
