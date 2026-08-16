package com.pureblog.tree.controller;

import com.pureblog.common.context.LoginUserHolder;
import com.pureblog.common.enums.ErrorCode;
import com.pureblog.common.exception.BusinessException;
import com.pureblog.common.result.ApiResponse;
import com.pureblog.tree.dto.*;
import com.pureblog.tree.entity.TreeDO;
import com.pureblog.tree.service.TreeService;
import com.pureblog.tree.vo.TreeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tree")
@RequiredArgsConstructor
public class TreeAdminController {

    private final TreeService treeService;

    @GetMapping("/list")
    public ApiResponse<List<TreeVO>> listAll() {
        requireAdmin();
        return ApiResponse.success(treeService.listAllTrees(true));
    }

    @PostMapping("/create")
    public ApiResponse<TreeDO> create(@Valid @RequestBody TreeCreateDTO dto) {
        requireAdmin();
        return ApiResponse.success(treeService.createTree(dto));
    }

    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody TreeUpdateDTO dto) {
        requireAdmin();
        treeService.updateTree(dto);
        return ApiResponse.success();
    }

    @PostMapping("/status/{id}")
    public ApiResponse<Void> updateStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status) {
        requireAdmin();
        treeService.updateTreeStatus(id, status);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        requireAdmin();
        treeService.deleteTree(id);
        return ApiResponse.success();
    }

    private void requireAdmin() {
        var user = LoginUserHolder.get();
        if (user == null || !user.isAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
