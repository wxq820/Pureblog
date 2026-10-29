package com.pureblog.article.controller;

import com.pureblog.article.dto.SearchRequest;
import com.pureblog.article.service.SearchService;
import com.pureblog.article.vo.SearchResult;
import com.pureblog.common.result.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 全文搜索接口 (MySQL LIKE 实现).
 *
 * <p>路径与响应结构与原 Elasticsearch 版本保持一致, 前端无需改动。
 * 原 ES 版的 {@code POST /index/create} 与 {@code POST /index/rebuild}
 * 已随索引方案一起下线 (LIKE 查询不需要索引)。
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ApiResponse<SearchResult> search(SearchRequest request) {
        if (request.getPage() == null || request.getPage() < 1) request.setPage(1);
        if (request.getSize() == null || request.getSize() < 1) request.setSize(10);
        return ApiResponse.success(searchService.search(request));
    }
}
