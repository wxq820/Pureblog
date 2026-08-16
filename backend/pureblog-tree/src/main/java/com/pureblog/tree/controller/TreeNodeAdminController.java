package com.pureblog.tree.controller;

import com.pureblog.common.context.LoginUserHolder;
import com.pureblog.common.enums.ErrorCode;
import com.pureblog.common.exception.BusinessException;
import com.pureblog.common.result.ApiResponse;
import com.pureblog.tree.dto.*;
import com.pureblog.tree.service.TreeService;
import com.pureblog.tree.vo.TreeNodeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tree-node")
@RequiredArgsConstructor
public class TreeNodeAdminController {

    private final TreeService treeService;

    @GetMapping("/list/{treeId}")
    public ApiResponse<List<TreeNodeVO>> listNodes(@PathVariable("treeId") Long treeId) {
        return ApiResponse.success(treeService.listNodesByTree(treeId));
    }

    @PostMapping("/create")
    public ApiResponse<Void> create(@Valid @RequestBody TreeNodeCreateDTO dto) {
        requireAdmin();
        treeService.createNode(dto);
        return ApiResponse.success();
    }

    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody TreeNodeUpdateDTO dto) {
        requireAdmin();
        treeService.updateNode(dto);
        return ApiResponse.success();
    }

    @PostMapping("/move")
    public ApiResponse<Void> move(@Valid @RequestBody TreeNodeMoveDTO dto) {
        requireAdmin();
        treeService.moveNode(dto);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        requireAdmin();
        treeService.deleteNode(id);
        return ApiResponse.success();
    }

    private void requireAdmin() {
        var user = LoginUserHolder.get();
        if (user == null || !user.isAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
