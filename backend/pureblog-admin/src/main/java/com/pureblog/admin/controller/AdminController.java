package com.pureblog.admin.controller;

import com.pureblog.admin.dto.ArticleAdminQueryDTO;
import com.pureblog.admin.dto.UserAdminDTO;
import com.pureblog.admin.service.AdminService;
import com.pureblog.admin.vo.AdminCommentVO;
import com.pureblog.admin.vo.AdminUserVO;
import com.pureblog.article.vo.ArticleListVO;
import com.pureblog.common.result.ApiResponse;
import com.pureblog.common.result.PageResult;
import com.pureblog.stats.vo.DashboardVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ApiResponse<DashboardVO> getDashboard() {
        return ApiResponse.success(adminService.getDashboard());
    }

    @GetMapping("/user/list")
    public ApiResponse<PageResult<AdminUserVO>> getUserList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(adminService.getUserList(page, size, keyword));
    }

    @PutMapping("/user/update")
    public ApiResponse<Void> updateUser(@Valid @RequestBody UserAdminDTO dto) {
        adminService.updateUser(dto);
        return ApiResponse.success();
    }

    @PostMapping("/user/disable/{userId}")
    public ApiResponse<Void> disableUser(@PathVariable Long userId) {
        adminService.disableUser(userId);
        return ApiResponse.success();
    }

    @GetMapping("/article/list")
    public ApiResponse<PageResult<ArticleListVO>> getArticleList(ArticleAdminQueryDTO query) {
        return ApiResponse.success(adminService.getArticleList(query));
    }

    @PostMapping("/article/offline/{id}")
    public ApiResponse<Void> offlineArticle(@PathVariable Long id) {
        adminService.offlineArticle(id);
        return ApiResponse.success();
    }

    @DeleteMapping("/article/{id}")
    public ApiResponse<Void> deleteArticle(@PathVariable Long id) {
        adminService.deleteArticle(id);
        return ApiResponse.success();
    }

    @GetMapping("/comment/pending")
    public ApiResponse<PageResult<AdminCommentVO>> getPendingComments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(adminService.getPendingComments(page, size));
    }

    @PostMapping("/comment/audit/{id}")
    public ApiResponse<Void> auditComment(@PathVariable Long id, @RequestParam boolean approve) {
        adminService.auditComment(id, approve);
        return ApiResponse.success();
    }

    @DeleteMapping("/comment/{id}")
    public ApiResponse<Void> deleteComment(@PathVariable Long id) {
        adminService.deleteComment(id);
        return ApiResponse.success();
    }
}
