package com.pureblog.tree.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TreeVO {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String coverColor;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 仅前台公开接口与后台详情接口包含此字段. */
    private TreeNodeVO root;
    /** 部分接口仅返回扁平节点 ID 列表,供下拉. */
    private List<TreeNodeVO> nodes;
}
