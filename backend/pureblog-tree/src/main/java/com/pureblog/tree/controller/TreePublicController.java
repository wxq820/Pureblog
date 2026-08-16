package com.pureblog.tree.controller;

import com.pureblog.common.result.ApiResponse;
import com.pureblog.tree.service.TreeService;
import com.pureblog.tree.vo.TreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tree")
@RequiredArgsConstructor
public class TreePublicController {

    private final TreeService treeService;

    /** 前台首页 tab 切换用: 列出所有启用的目录树 (扁平节点, 渲染前选择). */
    @GetMapping("/public/list")
    public ApiResponse<List<TreeVO>> listPublicTrees() {
        return ApiResponse.success(treeService.listAllTrees(true));
    }

    /** 前台首页 tab 选中后, 取这棵树的完整嵌套结构 (渲染 3D SkillTree). */
    @GetMapping("/public/{code}")
    public ApiResponse<TreeVO> getPublicTree(@PathVariable("code") String code) {
        return ApiResponse.success(treeService.getTreeByCode(code));
    }
}
