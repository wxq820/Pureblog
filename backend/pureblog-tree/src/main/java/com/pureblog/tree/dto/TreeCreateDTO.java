package com.pureblog.tree.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TreeCreateDTO {

    @NotBlank(message = "编码不能为空")
    @Size(max = 64, message = "编码长度不超过64")
    @Pattern(regexp = "^[a-z0-9_-]+$", message = "编码仅允许小写字母、数字、下划线、连字符")
    private String code;

    @NotBlank(message = "名称不能为空")
    @Size(max = 64, message = "名称长度不超过64")
    private String name;

    @Size(max = 255)
    private String description;

    private String coverColor;
    private Integer sortOrder;
}
