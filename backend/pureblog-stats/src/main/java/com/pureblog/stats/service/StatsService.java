package com.pureblog.stats.service;

import com.pureblog.stats.event.StatsEvent;
import com.pureblog.stats.vo.ArticleRankVO;
import com.pureblog.stats.vo.DashboardVO;
import java.util.List;

public interface StatsService {

    void handleStatsEvent(StatsEvent event);

    void incrementPv(Long articleId, String ip);

    DashboardVO getDashboardStats();

    List<ArticleRankVO> getHotArticles(int days, int limit);

    List<ArticleRankVO> getAuthorStats(Long authorId, int limit);

    void refreshHotArticles();
}
