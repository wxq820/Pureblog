package com.pureblog.tree.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TreeNodeCreateDTO {

    @NotNull(message = "treeId不能为空")
    private Long treeId;

    private Long parentId;

    @NotBlank(message = "节点名称不能为空")
    @Size(max = 128)
    private String name;

    private String color;
    private Integer sortOrder;
}
