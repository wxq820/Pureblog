package com.pureblog.widget.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WidgetUpdateDTO {

    @NotNull(message = "ID不能为空")
    private Long id;

    @Size(max = 64)
    private String name;

    @Size(max = 255)
    private String description;

    @Size(max = 32)
    private String icon;

    @Size(max = 16)
    private String iconColor;

    @Size(max = 128)
    private String path;

    private Integer sortOrder;
    private Integer status;
}
