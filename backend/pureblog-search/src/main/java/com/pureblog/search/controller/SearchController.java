package com.pureblog.search.controller;

import com.pureblog.common.result.ApiResponse;
import com.pureblog.search.dto.SearchRequest;
import com.pureblog.search.service.SearchService;
import com.pureblog.search.vo.SearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ApiResponse<SearchResult> search(SearchRequest request) {
        if (request.getPage() == null) request.setPage(1);
        if (request.getSize() == null) request.setSize(10);
        return ApiResponse.success(searchService.search(request));
    }

    @PostMapping("/index/create")
    public ApiResponse<Void> createIndex() {
        searchService.createIndex();
        return ApiResponse.success();
    }

    @PostMapping("/index/rebuild")
    public ApiResponse<Void> rebuildAllIndex() {
        searchService.rebuildAllIndex();
        return ApiResponse.success();
    }
}
