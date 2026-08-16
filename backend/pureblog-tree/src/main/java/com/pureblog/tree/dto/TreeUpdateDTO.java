package com.pureblog.tree.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TreeUpdateDTO {

    @NotNull(message = "ID不能为空")
    private Long id;

    @Size(max = 64)
    private String name;

    @Size(max = 255)
    private String description;

    private String coverColor;
    private Integer sortOrder;
    private Integer status;
}
