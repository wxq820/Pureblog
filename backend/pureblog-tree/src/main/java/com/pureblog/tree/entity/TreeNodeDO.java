package com.pureblog.tree.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pb_tree_node")
public class TreeNodeDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long treeId;
    private Long parentId;
    private String name;
    private String color;
    private Integer sortOrder;
    private Integer depth;
    private String path;
    private Integer articleCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
