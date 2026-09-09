package com.pureblog.article.controller;

import com.pureblog.article.entity.CategoryDO;
import com.pureblog.article.entity.TagDO;
import com.pureblog.article.service.ArticleService;
import com.pureblog.common.result.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class CategoryTagController {

    private final ArticleService articleService;

    @PostMapping("/category/create")
    public ApiResponse<Void> createCategory(@RequestBody CategoryDO category) {
        articleService.createCategory(category);
        return ApiResponse.success();
    }

    @PutMapping("/category/update")
    public ApiResponse<Void> updateCategory(@RequestBody CategoryDO category) {
        articleService.updateCategory(category);
        return ApiResponse.success();
    }

    @DeleteMapping("/category/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        articleService.deleteCategory(id);
        return ApiResponse.success();
    }

    @PostMapping("/tag/create")
    public ApiResponse<Void> createTag(@RequestBody TagDO tag) {
        articleService.createTag(tag);
        return ApiResponse.success();
    }

    @PutMapping("/tag/update")
    public ApiResponse<Void> updateTag(@RequestBody TagDO tag) {
        articleService.updateTag(tag);
        return ApiResponse.success();
    }

    @DeleteMapping("/tag/{id}")
    public ApiResponse<Void> deleteTag(@PathVariable Long id) {
        articleService.deleteTag(id);
        return ApiResponse.success();
    }
}
