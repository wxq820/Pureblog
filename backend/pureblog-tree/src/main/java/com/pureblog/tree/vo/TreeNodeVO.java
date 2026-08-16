package com.pureblog.tree.vo;

import lombok.Data;

import java.util.List;

@Data
public class TreeNodeVO {

    private Long id;
    private Long treeId;
    private Long parentId;
    private String name;
    private String color;
    private Integer sortOrder;
    private Integer depth;
    private Integer articleCount;
    private Boolean leaf;
    private List<TreeNodeVO> children;
}
