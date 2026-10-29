package com.pureblog.widget.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WidgetCreateDTO {

    @NotBlank(message = "编码不能为空")
    @Size(max = 64)
    @Pattern(regexp = "^[a-z0-9_-]+$", message = "编码仅允许小写字母、数字、下划线、连字符")
    private String code;

    @NotBlank(message = "名称不能为空")
    @Size(max = 64)
    private String name;

    @Size(max = 255)
    private String description;

    @Size(max = 32)
    private String icon;

    @Size(max = 16)
    private String iconColor;

    @NotBlank(message = "路径不能为空")
    @Size(max = 128)
    private String path;

    private Integer sortOrder;
    private Integer status;
}
