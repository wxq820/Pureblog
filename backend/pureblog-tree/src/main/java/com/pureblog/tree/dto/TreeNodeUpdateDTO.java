package com.pureblog.tree.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TreeNodeUpdateDTO {

    @NotNull(message = "节点ID不能为空")
    private Long id;

    @Size(max = 128)
    private String name;

    private String color;
    private Integer sortOrder;
}
