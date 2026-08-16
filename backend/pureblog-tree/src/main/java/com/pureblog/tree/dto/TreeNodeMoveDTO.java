package com.pureblog.tree.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TreeNodeMoveDTO {

    @NotNull(message = "节点ID不能为空")
    private Long id;

    @NotNull(message = "目标 parentId 不能为空 (根节点传 0)")
    private Long newParentId;

    private Integer newSortOrder;
}
