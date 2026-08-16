package com.pureblog.tree.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pureblog.tree.entity.TreeNodeDO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TreeNodeMapper extends BaseMapper<TreeNodeDO> {

    @Select("SELECT COUNT(*) FROM pb_tree_node WHERE tree_id = #{treeId} AND parent_id = #{parentId} AND deleted = 0")
    long countByParent(Long treeId, Long parentId);

    @Select("SELECT COUNT(*) FROM pb_tree_node WHERE tree_id = #{treeId} AND path LIKE CONCAT(#{path}, '%') AND deleted = 0")
    long countDescendants(Long treeId, String path);
}
