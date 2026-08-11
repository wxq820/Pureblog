package com.pureblog.comment.controller;

import com.pureblog.comment.dto.CommentCreateDTO;
import com.pureblog.comment.service.CommentService;
import com.pureblog.comment.vo.ArticleStatsVO;
import com.pureblog.comment.vo.CommentVO;
import com.pureblog.common.result.ApiResponse;
import com.pureblog.common.result.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/comment/create")
    public ApiResponse<CommentVO> createComment(@Valid @RequestBody CommentCreateDTO dto) {
        return ApiResponse.success(commentService.createComment(dto));
    }

    @DeleteMapping("/comment/{id}")
    public ApiResponse<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ApiResponse.success();
    }

    @GetMapping("/comment/article/{articleId}")
    public ApiResponse<PageResult<CommentVO>> getArticleComments(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(commentService.getArticleComments(articleId, page, size));
    }

    @PostMapping("/article/like/{articleId}")
    public ApiResponse<Void> likeArticle(@PathVariable Long articleId) {
        commentService.likeArticle(articleId);
        return ApiResponse.success();
    }

    @DeleteMapping("/article/like/{articleId}")
    public ApiResponse<Void> unlikeArticle(@PathVariable Long articleId) {
        commentService.unlikeArticle(articleId);
        return ApiResponse.success();
    }

    @PostMapping("/article/collect/{articleId}")
    public ApiResponse<Void> collectArticle(@PathVariable Long articleId) {
        commentService.collectArticle(articleId);
        return ApiResponse.success();
    }

    @DeleteMapping("/article/collect/{articleId}")
    public ApiResponse<Void> uncollectArticle(@PathVariable Long articleId) {
        commentService.uncollectArticle(articleId);
        return ApiResponse.success();
    }

    @GetMapping("/article/stats/{articleId}")
    public ApiResponse<ArticleStatsVO> getArticleStats(@PathVariable Long articleId) {
        return ApiResponse.success(commentService.getArticleStats(articleId));
    }
}
