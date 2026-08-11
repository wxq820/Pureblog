package com.pureblog.stats.controller;

import com.pureblog.common.result.ApiResponse;
import com.pureblog.stats.service.StatsService;
import com.pureblog.stats.vo.ArticleRankVO;
import com.pureblog.stats.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/stats/dashboard")
    public ApiResponse<DashboardVO> getDashboard() {
        return ApiResponse.success(statsService.getDashboardStats());
    }

    @GetMapping("/stats/hot")
    public ApiResponse<List<ArticleRankVO>> getHotArticles(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(statsService.getHotArticles(days, limit));
    }

    @GetMapping("/stats/author/{authorId}")
    public ApiResponse<List<ArticleRankVO>> getAuthorStats(
            @PathVariable Long authorId,
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(statsService.getAuthorStats(authorId, limit));
    }

    @PostMapping("/stats/pv")
    public ApiResponse<Void> recordPv(@RequestBody Map<String, Object> body) {
        Long articleId = Long.valueOf(body.get("articleId").toString());
        String ip = body.get("ip") != null ? body.get("ip").toString() : null;
        statsService.incrementPv(articleId, ip);
        return ApiResponse.success();
    }
}
