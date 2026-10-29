package com.pureblog.widget.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LedgerDictCreateDTO {

    /** 1=平台 2=项目 */
    @NotNull(message = "类型不能为空")
    private Integer type;

    @NotBlank(message = "名称不能为空")
    @Size(max = 64)
    private String name;

    @Size(max = 16)
    private String color;

    @Size(max = 32)
    private String icon;
}
