package com.pureblog.article.controller;

import com.pureblog.article.dto.*;
import com.pureblog.article.service.ArticleService;
import com.pureblog.article.vo.*;
import com.pureblog.common.result.ApiResponse;
import com.pureblog.common.result.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping("/article/create")
    public ApiResponse<ArticleDetailVO> createArticle(@Valid @RequestBody ArticleCreateDTO dto) {
        return ApiResponse.success(articleService.createArticle(dto));
    }

    @PutMapping("/article/update")
    public ApiResponse<ArticleDetailVO> updateArticle(@Valid @RequestBody ArticleUpdateDTO dto) {
        return ApiResponse.success(articleService.updateArticle(dto));
    }

    @PostMapping("/article/publish")
    public ApiResponse<Void> publishArticle(@Valid @RequestBody ArticlePublishDTO dto) {
        articleService.publishArticle(dto);
        return ApiResponse.success();
    }

    @PostMapping("/article/offline/{id}")
    public ApiResponse<Void> offlineArticle(@PathVariable Long id) {
        articleService.offlineArticle(id);
        return ApiResponse.success();
    }

    @DeleteMapping("/article/{id}")
    public ApiResponse<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ApiResponse.success();
    }

    @GetMapping("/article/public/{id}")
    public ApiResponse<ArticleDetailVO> getArticleDetail(@PathVariable Long id) {
        return ApiResponse.success(articleService.getArticleDetail(id));
    }

    @GetMapping("/article/list")
    public ApiResponse<PageResult<ArticleListVO>> getArticleList(ArticleQueryDTO query) {
        return ApiResponse.success(articleService.getArticleList(query));
    }

    @GetMapping("/article/hot")
    public ApiResponse<List<ArticleListVO>> getHotArticles(@RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(articleService.getHotArticles(limit));
    }

    @GetMapping("/article/featured")
    public ApiResponse<List<ArticleListVO>> getFeaturedArticles(@RequestParam(defaultValue = "5") int limit) {
        return ApiResponse.success(articleService.getFeaturedArticles(limit));
    }

    @GetMapping("/article/author/{authorId}")
    public ApiResponse<List<ArticleListVO>> getAuthorArticles(
            @PathVariable Long authorId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(articleService.getAuthorArticles(authorId, page, size));
    }

    @GetMapping("/category/list")
    public ApiResponse<List<CategoryVO>> getAllCategories() {
        return ApiResponse.success(articleService.getAllCategories());
    }

    @GetMapping("/tag/list")
    public ApiResponse<List<TagVO>> getAllTags() {
        return ApiResponse.success(articleService.getAllTags());
    }
}
