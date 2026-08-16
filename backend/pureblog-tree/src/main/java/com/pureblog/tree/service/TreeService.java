package com.pureblog.tree.service;

import com.pureblog.tree.dto.*;
import com.pureblog.tree.entity.TreeDO;
import com.pureblog.tree.entity.TreeNodeDO;
import com.pureblog.tree.vo.TreeNodeVO;
import com.pureblog.tree.vo.TreeVO;

import java.util.List;

public interface TreeService {

    /* ============ Tree (目录树主表) ============ */

    List<TreeVO> listAllTrees(boolean withNodes);

    TreeVO getTreeByCode(String code);

    TreeDO createTree(TreeCreateDTO dto);

    void updateTree(TreeUpdateDTO dto);

    void updateTreeStatus(Long treeId, Integer status);

    void deleteTree(Long treeId);

    /* ============ TreeNode (目录树节点) ============ */

    List<TreeNodeVO> listNodesByTree(Long treeId);

    TreeNodeDO getNodeById(Long nodeId);

    /** 检查 nodeId 必须是叶子节点,否则抛 TREE_NODE_NOT_LEAF. */
    void assertIsLeaf(Long nodeId);

    /** 叶子节点 -1 (写文章删除时调用) */
    void decrementArticleCount(Long nodeId);

    /** 叶子节点 +1 (写文章新增时调用) */
    void incrementArticleCount(Long nodeId);

    TreeNodeDO createNode(TreeNodeCreateDTO dto);

    void updateNode(TreeNodeUpdateDTO dto);

    void moveNode(TreeNodeMoveDTO dto);

    void deleteNode(Long nodeId);
}
