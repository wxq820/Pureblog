package com.pureblog.admin.controller;

import com.pureblog.admin.dto.AdminArticleCreateDTO;
import com.pureblog.admin.dto.AdminArticleUpdateDTO;
import com.pureblog.admin.dto.ArticleAdminQueryDTO;
import com.pureblog.admin.dto.UserAdminDTO;
import com.pureblog.admin.service.AdminService;
import com.pureblog.admin.vo.AdminCommentVO;
import com.pureblog.admin.vo.AdminUserVO;
import com.pureblog.article.dto.ArticlePublishDTO;
import com.pureblog.article.service.ArticleService;
import com.pureblog.article.vo.ArticleDetailVO;
import com.pureblog.article.vo.ArticleListVO;
import com.pureblog.common.result.ApiResponse;
import com.pureblog.common.result.PageResult;
import com.pureblog.stats.vo.DashboardVO;
import com.pureblog.tree.service.TreeService;
import com.pureblog.tree.vo.TreeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ArticleService articleService;
    private final TreeService treeService;

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

    @PostMapping("/article/create")
    public ApiResponse<ArticleDetailVO> adminCreateArticle(@Valid @RequestBody AdminArticleCreateDTO dto) {
        adminService.requireAdmin();
        com.pureblog.article.dto.ArticleCreateDTO create = new com.pureblog.article.dto.ArticleCreateDTO();
        create.setTitle(dto.getTitle());
        create.setSummary(dto.getSummary());
        create.setCoverUrl(dto.getCoverUrl());
        create.setCategoryId(dto.getCategoryId());
        create.setTreeNodeId(dto.getTreeNodeId());
        create.setTagIds(dto.getTagIds());
        create.setContent(dto.getContent());
        create.setHtmlContent(dto.getHtmlContent());
        ArticleDetailVO created = articleService.createArticle(create);
        // admin 创建文章默认立即发布,走发布管道 (索引同步 / 通知).
        com.pureblog.article.dto.ArticlePublishDTO publish = new com.pureblog.article.dto.ArticlePublishDTO();
        publish.setId(created.getId());
        publish.setIsFeatured(0);
        publish.setIsTop(0);
        articleService.publishArticle(publish);
        return ApiResponse.success(created);
    }

    @PutMapping("/article/update")
    public ApiResponse<ArticleDetailVO> adminUpdateArticle(@Valid @RequestBody AdminArticleUpdateDTO dto) {
        adminService.requireAdmin();
        com.pureblog.article.dto.ArticleUpdateDTO update = new com.pureblog.article.dto.ArticleUpdateDTO();
        update.setId(dto.getId());
        update.setTitle(dto.getTitle());
        update.setSummary(dto.getSummary());
        update.setCoverUrl(dto.getCoverUrl());
        update.setCategoryId(dto.getCategoryId());
        update.setTreeNodeId(dto.getTreeNodeId());
        update.setTagIds(dto.getTagIds());
        update.setContent(dto.getContent());
        update.setHtmlContent(dto.getHtmlContent());
        return ApiResponse.success(articleService.updateArticle(update));
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

    @GetMapping("/article/tree-list")
    public ApiResponse<List<TreeVO>> listAllTreesForArticle() {
        adminService.requireAdmin();
        return ApiResponse.success(treeService.listAllTrees(true));
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
