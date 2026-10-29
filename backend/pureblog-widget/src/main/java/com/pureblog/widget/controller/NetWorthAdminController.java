package com.pureblog.widget.controller;

import com.pureblog.common.context.LoginUserHolder;
import com.pureblog.common.enums.ErrorCode;
import com.pureblog.common.exception.BusinessException;
import com.pureblog.common.result.ApiResponse;
import com.pureblog.widget.dto.AccountCategoryCreateDTO;
import com.pureblog.widget.dto.AccountCreateDTO;
import com.pureblog.widget.entity.AccountCategoryDO;
import com.pureblog.widget.entity.AccountDO;
import com.pureblog.widget.service.NetWorthService;
import com.pureblog.widget.vo.AccountCategoryVO;
import com.pureblog.widget.vo.AccountVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 身家 widget - admin 后台. 分类/账户 CRUD.
 * seed 3 个分类 (id 1/2/3) 不可删, 后端强校验.
 */
@RestController
@RequestMapping("/api/admin/widget/networth")
@RequiredArgsConstructor
public class NetWorthAdminController {

    private final NetWorthService netWorthService;

    // -------- 分类 --------

    @GetMapping("/category/list")
    public ApiResponse<List<AccountCategoryVO>> listCategories() {
        requireAdmin();
        return ApiResponse.success(netWorthService.listAllCategories());
    }

    @PostMapping("/category/create")
    public ApiResponse<AccountCategoryDO> createCategory(@Valid @RequestBody AccountCategoryCreateDTO dto) {
        requireAdmin();
        return ApiResponse.success(netWorthService.createCategory(dto));
    }

    @PutMapping("/category/update/{id}")
    public ApiResponse<Void> updateCategory(@PathVariable("id") Long id, @Valid @RequestBody AccountCategoryCreateDTO dto) {
        requireAdmin();
        netWorthService.updateCategory(id, dto);
        return ApiResponse.success();
    }

    @DeleteMapping("/category/delete/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable("id") Long id) {
        requireAdmin();
        netWorthService.deleteCategory(id);
        return ApiResponse.success();
    }

    // -------- 账户 --------

    @GetMapping("/account/list")
    public ApiResponse<List<AccountVO>> listAccounts() {
        requireAdmin();
        return ApiResponse.success(netWorthService.listAllAccounts());
    }

    @PostMapping("/account/create")
    public ApiResponse<AccountDO> createAccount(@Valid @RequestBody AccountCreateDTO dto) {
        requireAdmin();
        return ApiResponse.success(netWorthService.createAccount(dto));
    }

    @PutMapping("/account/update/{id}")
    public ApiResponse<Void> updateAccount(@PathVariable("id") Long id, @Valid @RequestBody AccountCreateDTO dto) {
        requireAdmin();
        netWorthService.updateAccount(id, dto);
        return ApiResponse.success();
    }

    @DeleteMapping("/account/delete/{id}")
    public ApiResponse<Void> deleteAccount(@PathVariable("id") Long id) {
        requireAdmin();
        netWorthService.deleteAccount(id);
        return ApiResponse.success();
    }

    private void requireAdmin() {
        var user = LoginUserHolder.get();
        if (user == null || !user.isAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
