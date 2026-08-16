package com.pureblog.stats.service;

import com.pureblog.stats.vo.ArticleRankVO;
import com.pureblog.stats.vo.DashboardVO;

import java.util.List;

public interface StatsService {

    void incrementPv(Long articleId, String ip);

    void recordLikeHotScore(Long articleId);

    DashboardVO getDashboardStats();

    List<ArticleRankVO> getHotArticles(int days, int limit);

    List<ArticleRankVO> getAuthorStats(Long authorId, int limit);

    void refreshHotArticles();
}