package com.pureblog.tree.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pureblog.common.enums.ErrorCode;
import com.pureblog.common.exception.BusinessException;
import com.pureblog.common.utils.StringUtils;
import com.pureblog.tree.dto.*;
import com.pureblog.tree.entity.TreeDO;
import com.pureblog.tree.entity.TreeNodeDO;
import com.pureblog.tree.mapper.TreeMapper;
import com.pureblog.tree.mapper.TreeNodeMapper;
import com.pureblog.tree.service.TreeService;
import com.pureblog.tree.vo.TreeNodeVO;
import com.pureblog.tree.vo.TreeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TreeServiceImpl implements TreeService {

    private final TreeMapper treeMapper;
    private final TreeNodeMapper treeNodeMapper;

    /* ============== Tree 主表 CRUD ============== */

    @Override
    public List<TreeVO> listAllTrees(boolean withNodes) {
        List<TreeDO> trees = treeMapper.selectList(new LambdaQueryWrapper<TreeDO>()
                .eq(TreeDO::getDeleted, 0)
                .orderByAsc(TreeDO::getSortOrder));
        return trees.stream().map(t -> {
            TreeVO vo = toTreeVO(t);
            if (withNodes) {
                vo.setNodes(buildFlatNodes(t.getId()));
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public TreeVO getTreeByCode(String code) {
        TreeDO tree = treeMapper.selectOne(new LambdaQueryWrapper<TreeDO>()
                .eq(TreeDO::getCode, code)
                .eq(TreeDO::getDeleted, 0)
                .eq(TreeDO::getStatus, 1));
        if (tree == null) throw new BusinessException(ErrorCode.TREE_NOT_FOUND);
        TreeVO vo = toTreeVO(tree);
        vo.setRoot(buildTreeRecursive(tree.getId()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TreeDO createTree(TreeCreateDTO dto) {
        Long existed = treeMapper.selectCount(new LambdaQueryWrapper<TreeDO>()
                .eq(TreeDO::getCode, dto.getCode())
                .eq(TreeDO::getDeleted, 0));
        if (existed != null && existed > 0) throw new BusinessException(ErrorCode.TREE_CODE_EXISTS);

        TreeDO tree = new TreeDO();
        tree.setCode(dto.getCode());
        tree.setName(dto.getName());
        tree.setDescription(dto.getDescription());
        tree.setCoverColor(StringUtils.isBlank(dto.getCoverColor()) ? "#2563eb" : dto.getCoverColor());
        tree.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        tree.setStatus(1);
        treeMapper.insert(tree);

        // 自动创建根节点.
        TreeNodeCreateDTO root = new TreeNodeCreateDTO();
        root.setTreeId(tree.getId());
        root.setParentId(0L);
        root.setName(dto.getName());
        root.setColor(tree.getCoverColor());
        root.setSortOrder(0);
        createNode(root);

        log.info("Tree created: id={}, code={}", tree.getId(), tree.getCode());
        return tree;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTree(TreeUpdateDTO dto) {
        TreeDO tree = treeMapper.selectById(dto.getId());
        if (tree == null) throw new BusinessException(ErrorCode.TREE_NOT_FOUND);
        if (StringUtils.isNotBlank(dto.getName())) tree.setName(dto.getName());
        if (dto.getDescription() != null) tree.setDescription(dto.getDescription());
        if (StringUtils.isNotBlank(dto.getCoverColor())) tree.setCoverColor(dto.getCoverColor());
        if (dto.getSortOrder() != null) tree.setSortOrder(dto.getSortOrder());
        if (dto.getStatus() != null) tree.setStatus(dto.getStatus());
        treeMapper.updateById(tree);
        log.info("Tree updated: id={}", dto.getId());
    }

    @Override
    public void updateTreeStatus(Long treeId, Integer status) {
        TreeDO tree = treeMapper.selectById(treeId);
        if (tree == null) throw new BusinessException(ErrorCode.TREE_NOT_FOUND);
        treeMapper.update(null, new LambdaUpdateWrapper<TreeDO>()
                .eq(TreeDO::getId, treeId)
                .set(TreeDO::getStatus, status));
        log.info("Tree status updated: id={}, status={}", treeId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTree(Long treeId) {
        TreeDO tree = treeMapper.selectById(treeId);
        if (tree == null) throw new BusinessException(ErrorCode.TREE_NOT_FOUND);
        long nodeCount = treeNodeMapper.selectCount(new LambdaQueryWrapper<TreeNodeDO>()
                .eq(TreeNodeDO::getTreeId, treeId).eq(TreeNodeDO::getDeleted, 0));
        if (nodeCount > 1) {
            throw new BusinessException(ErrorCode.TREE_NODE_HAS_CHILDREN);
        }
        treeMapper.deleteById(treeId);
        treeNodeMapper.delete(new LambdaQueryWrapper<TreeNodeDO>()
                .eq(TreeNodeDO::getTreeId, treeId));
        log.info("Tree deleted: id={}", treeId);
    }

    /* ============== TreeNode 节点操作 ============== */

    @Override
    public List<TreeNodeVO> listNodesByTree(Long treeId) {
        return buildFlatNodes(treeId);
    }

    @Override
    public TreeNodeDO getNodeById(Long nodeId) {
        TreeNodeDO node = treeNodeMapper.selectById(nodeId);
        if (node == null) throw new BusinessException(ErrorCode.TREE_NODE_NOT_FOUND);
        return node;
    }

    @Override
    public void assertIsLeaf(Long nodeId) {
        if (nodeId == null) throw new BusinessException(ErrorCode.TREE_NODE_NOT_FOUND);
        long children = treeNodeMapper.countByParent(getNodeById(nodeId).getTreeId(), nodeId);
        if (children > 0) throw new BusinessException(ErrorCode.TREE_NODE_NOT_LEAF);
    }

    @Override
    public void incrementArticleCount(Long nodeId) {
        if (nodeId == null) return;
        treeNodeMapper.update(null, new LambdaUpdateWrapper<TreeNodeDO>()
                .eq(TreeNodeDO::getId, nodeId)
                .setSql("article_count = article_count + 1"));
    }

    @Override
    public void decrementArticleCount(Long nodeId) {
        if (nodeId == null) return;
        treeNodeMapper.update(null, new LambdaUpdateWrapper<TreeNodeDO>()
                .eq(TreeNodeDO::getId, nodeId)
                .setSql("article_count = GREATEST(article_count - 1, 0)"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TreeNodeDO createNode(TreeNodeCreateDTO dto) {
        TreeDO tree = treeMapper.selectById(dto.getTreeId());
        if (tree == null) throw new BusinessException(ErrorCode.TREE_NOT_FOUND);

        TreeNodeDO node = new TreeNodeDO();
        node.setTreeId(dto.getTreeId());
        Long parentId = dto.getParentId();
        int depth = 0;
        String path = "/";
        if (parentId != null && parentId > 0) {
            TreeNodeDO parent = treeNodeMapper.selectById(parentId);
            if (parent == null) throw new BusinessException(ErrorCode.TREE_NODE_NOT_FOUND);
            depth = parent.getDepth() + 1;
            path = parent.getPath();
        }
        node.setParentId(parentId);
        node.setName(dto.getName());
        node.setColor(dto.getColor());
        node.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        node.setDepth(depth);
        node.setArticleCount(0);
        treeNodeMapper.insert(node);

        node.setPath(path + node.getId() + "/");
        treeNodeMapper.updateById(node);
        log.info("Tree node created: id={}, treeId={}, parentId={}", node.getId(), node.getTreeId(), node.getParentId());
        return node;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNode(TreeNodeUpdateDTO dto) {
        TreeNodeDO node = treeNodeMapper.selectById(dto.getId());
        if (node == null) throw new BusinessException(ErrorCode.TREE_NODE_NOT_FOUND);
        if (StringUtils.isNotBlank(dto.getName())) node.setName(dto.getName());
        if (dto.getColor() != null) node.setColor(dto.getColor());
        if (dto.getSortOrder() != null) node.setSortOrder(dto.getSortOrder());
        treeNodeMapper.updateById(node);
        log.info("Tree node updated: id={}", dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveNode(TreeNodeMoveDTO dto) {
        TreeNodeDO node = treeNodeMapper.selectById(dto.getId());
        if (node == null) throw new BusinessException(ErrorCode.TREE_NODE_NOT_FOUND);
        if (dto.getNewParentId() == null || dto.getNewParentId() == 0L) {
            node.setParentId(0L);
            node.setDepth(0);
        } else {
            TreeNodeDO parent = treeNodeMapper.selectById(dto.getNewParentId());
            if (parent == null) throw new BusinessException(ErrorCode.TREE_NODE_NOT_FOUND);
            if (!parent.getTreeId().equals(node.getTreeId())) {
                throw new BusinessException(ErrorCode.PARAM_INVALID, "只能在同一棵树下移动");
            }
            // 防止把节点移动到自己的后代里.
            String childPathPrefix = node.getPath();
            if (parent.getPath() != null && parent.getPath().startsWith(childPathPrefix)) {
                throw new BusinessException(ErrorCode.PARAM_INVALID, "不能把节点移到自己的子树下");
            }
            node.setParentId(parent.getId());
            node.setDepth(parent.getDepth() + 1);
        }
        if (dto.getNewSortOrder() != null) node.setSortOrder(dto.getNewSortOrder());
        treeNodeMapper.updateById(node);
        refreshSubtreePaths(node);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNode(Long nodeId) {
        TreeNodeDO node = treeNodeMapper.selectById(nodeId);
        if (node == null) throw new BusinessException(ErrorCode.TREE_NODE_NOT_FOUND);
        long children = treeNodeMapper.countByParent(node.getTreeId(), nodeId);
        if (children > 0) throw new BusinessException(ErrorCode.TREE_NODE_HAS_CHILDREN);
        if (node.getArticleCount() != null && node.getArticleCount() > 0) {
            throw new BusinessException(ErrorCode.TREE_NODE_HAS_ARTICLES);
        }
        treeNodeMapper.deleteById(nodeId);
        log.info("Tree node deleted: id={}", nodeId);
    }

    /* ============== 内部辅助 ============== */

    private TreeVO toTreeVO(TreeDO tree) {
        TreeVO vo = new TreeVO();
        vo.setId(tree.getId());
        vo.setCode(tree.getCode());
        vo.setName(tree.getName());
        vo.setDescription(tree.getDescription());
        vo.setCoverColor(tree.getCoverColor());
        vo.setSortOrder(tree.getSortOrder());
        vo.setStatus(tree.getStatus());
        vo.setCreatedAt(tree.getCreatedAt());
        vo.setUpdatedAt(tree.getUpdatedAt());
        return vo;
    }

    private List<TreeNodeVO> buildFlatNodes(Long treeId) {
        List<TreeNodeDO> all = treeNodeMapper.selectList(new LambdaQueryWrapper<TreeNodeDO>()
                .eq(TreeNodeDO::getTreeId, treeId)
                .eq(TreeNodeDO::getDeleted, 0)
                .orderByAsc(TreeNodeDO::getDepth, TreeNodeDO::getSortOrder));
        return all.stream().map(this::toNodeVO).collect(Collectors.toList());
    }

    private TreeNodeVO buildTreeRecursive(Long treeId) {
        List<TreeNodeDO> all = treeNodeMapper.selectList(new LambdaQueryWrapper<TreeNodeDO>()
                .eq(TreeNodeDO::getTreeId, treeId)
                .eq(TreeNodeDO::getDeleted, 0)
                .orderByAsc(TreeNodeDO::getDepth, TreeNodeDO::getSortOrder));
        Map<Long, TreeNodeVO> map = new LinkedHashMap<>();
        TreeNodeVO root = null;
        for (TreeNodeDO n : all) {
            TreeNodeVO vo = toNodeVO(n);
            map.put(vo.getId(), vo);
        }
        for (TreeNodeDO n : all) {
            TreeNodeVO vo = map.get(n.getId());
            Long pid = n.getParentId();
            if (pid == null || pid == 0L) {
                if (root == null) root = vo;
            } else {
                TreeNodeVO parent = map.get(pid);
                if (parent != null) {
                    if (parent.getChildren() == null) parent.setChildren(new ArrayList<>());
                    parent.getChildren().add(vo);
                }
            }
        }
        return root;
    }

    private TreeNodeVO toNodeVO(TreeNodeDO n) {
        TreeNodeVO vo = new TreeNodeVO();
        vo.setId(n.getId());
        vo.setTreeId(n.getTreeId());
        vo.setParentId(n.getParentId() == null ? 0L : n.getParentId());
        vo.setName(n.getName());
        vo.setColor(n.getColor());
        vo.setSortOrder(n.getSortOrder());
        vo.setDepth(n.getDepth());
        vo.setArticleCount(n.getArticleCount());
        return vo;
    }

    /** 把节点 n 及其所有后代的 path/depth 按新位置重算. */
    private void refreshSubtreePaths(TreeNodeDO n) {
        if (n == null || n.getPath() == null) return;
        if (n.getParentId() == null || n.getParentId() == 0L) {
            n.setPath("/" + n.getId() + "/");
            n.setDepth(0);
            treeNodeMapper.updateById(n);
        } else {
            TreeNodeDO parent = treeNodeMapper.selectById(n.getParentId());
            if (parent == null) return;
            n.setPath(parent.getPath() + n.getId() + "/");
            n.setDepth(parent.getDepth() + 1);
            treeNodeMapper.updateById(n);
        }
        List<TreeNodeDO> children = treeNodeMapper.selectList(new LambdaQueryWrapper<TreeNodeDO>()
                .eq(TreeNodeDO::getParentId, n.getId())
                .eq(TreeNodeDO::getDeleted, 0));
        for (TreeNodeDO c : children) {
            refreshSubtreePaths(c);
        }
    }
}
