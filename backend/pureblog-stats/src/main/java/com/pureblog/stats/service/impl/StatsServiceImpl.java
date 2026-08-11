package com.pureblog.stats.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pureblog.article.entity.ArticleDO;
import com.pureblog.article.mapper.ArticleMapper;
import com.pureblog.auth.mapper.UserMapper;
import com.pureblog.common.enums.ArticleStatus;
import com.pureblog.stats.entity.ArticleStatsDO;
import com.pureblog.stats.event.StatsEvent;
import com.pureblog.stats.mapper.ArticleStatsMapper;
import com.pureblog.stats.service.StatsService;
import com.pureblog.stats.vo.ArticleRankVO;
import com.pureblog.stats.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final ArticleMapper articleMapper;
    private final ArticleStatsMapper statsMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void handleStatsEvent(StatsEvent event) {
        switch (event.getEventType()) {
            case "PV" -> incrementPv(event.getArticleId(), event.getIp());
            case "LIKE_ARTICLE" -> updateHotScore(event.getArticleId(), 10);
            case "COMMENT_ARTICLE" -> updateHotScore(event.getArticleId(), 5);
            case "COLLECT_ARTICLE" -> updateHotScore(event.getArticleId(), 8);
            default -> log.warn("Unknown stats event type: {}", event.getEventType());
        }
    }

    @Override
    public void incrementPv(Long articleId, String ip) {
        String pvKey = "pureblog:article:pv:" + articleId;
        redisTemplate.opsForValue().increment(pvKey);
        redisTemplate.expire(pvKey, 2, TimeUnit.DAYS);

        if (ip != null) {
            String uvKey = "pureblog:article:uv:" + articleId + ":" + LocalDate.now();
            redisTemplate.opsForSet().add(uvKey, ip);
            redisTemplate.expire(uvKey, 2, TimeUnit.DAYS);
        }

        updateHotScore(articleId, 1);
    }

    private void updateHotScore(Long articleId, int delta) {
        String hotKey = "pureblog:article:hot:zset";
        Double currentScore = redisTemplate.opsForZSet().score(hotKey, String.valueOf(articleId));
        double newScore = (currentScore != null ? currentScore : 0) + delta;
        redisTemplate.opsForZSet().add(hotKey, String.valueOf(articleId), newScore);
        redisTemplate.expire(hotKey, 7, TimeUnit.DAYS);
    }

    @Override
    public DashboardVO getDashboardStats() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        long totalArticles = articleMapper.selectCount(new LambdaQueryWrapper<ArticleDO>()
                .eq(ArticleDO::getStatus, ArticleStatus.PUBLISHED.getCode()));
        long totalUsers = userMapper.selectCount(null);

        long totalComments = articleMapper.selectList(null).stream()
                .mapToInt(ArticleDO::getCommentCount)
                .sum();

        long totalViews = articleMapper.selectList(null).stream()
                .mapToInt(ArticleDO::getViewCount)
                .sum();

        long todayArticles = articleMapper.selectCount(new LambdaQueryWrapper<ArticleDO>()
                .eq(ArticleDO::getStatus, ArticleStatus.PUBLISHED.getCode())
                .ge(ArticleDO::getPublishedAt, startOfDay)
                .lt(ArticleDO::getPublishedAt, endOfDay));

        return DashboardVO.builder()
                .totalArticles(totalArticles)
                .totalUsers(totalUsers)
                .totalComments(totalComments)
                .totalViews(totalViews)
                .todayViews(0L)
                .todayArticles(todayArticles)
                .todayComments(0L)
                .pendingComments(0L)
                .build();
    }

    @Override
    public List<ArticleRankVO> getHotArticles(int days, int limit) {
        String hotKey = "pureblog:article:hot:zset";
        Set<ZSetOperations.TypedTuple<Object>> topSet = redisTemplate.opsForZSet()
                .reverseRangeWithScores(hotKey, 0, limit - 1);

        if (topSet == null || topSet.isEmpty()) {
            return new ArrayList<>();
        }

        List<ArticleRankVO> result = new ArrayList<>();
        for (ZSetOperations.TypedTuple<Object> tuple : topSet) {
            Long articleId = Long.parseLong(tuple.getValue().toString());
            ArticleDO article = articleMapper.selectByIdSimple(articleId);
            if (article != null && article.getStatus() == ArticleStatus.PUBLISHED.getCode()) {
                result.add(ArticleRankVO.builder()
                        .articleId(articleId)
                        .title(article.getTitle())
                        .viewCount(article.getViewCount())
                        .likeCount(article.getLikeCount())
                        .commentCount(article.getCommentCount())
                        .hotScore(tuple.getScore() != null ? tuple.getScore().intValue() : 0)
                        .build());
            }
        }
        return result;
    }

    @Override
    public List<ArticleRankVO> getAuthorStats(Long authorId, int limit) {
        LambdaQueryWrapper<ArticleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleDO::getAuthorId, authorId)
                .eq(ArticleDO::getStatus, ArticleStatus.PUBLISHED.getCode())
                .orderByDesc(ArticleDO::getViewCount)
                .last("LIMIT " + limit);

        return articleMapper.selectList(wrapper).stream().map(a -> ArticleRankVO.builder()
                .articleId(a.getId())
                .title(a.getTitle())
                .viewCount(a.getViewCount())
                .likeCount(a.getLikeCount())
                .commentCount(a.getCommentCount())
                .hotScore(a.getViewCount() + a.getLikeCount() * 10 + a.getCommentCount() * 5)
                .build()).collect(Collectors.toList());
    }

    @Scheduled(fixedRate = 300000)
    @Override
    public void refreshHotArticles() {
        log.info("Refreshing hot articles score from database...");
        LambdaQueryWrapper<ArticleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleDO::getStatus, ArticleStatus.PUBLISHED.getCode());
        List<ArticleDO> articles = articleMapper.selectList(wrapper);

        String hotKey = "pureblog:article:hot:zset";
        redisTemplate.delete(hotKey);

        for (ArticleDO article : articles) {
            int score = article.getViewCount() + article.getLikeCount() * 10 + article.getCommentCount() * 5;
            redisTemplate.opsForZSet().add(hotKey, String.valueOf(article.getId()), score);
        }

        redisTemplate.expire(hotKey, 7, TimeUnit.DAYS);
        log.info("Hot articles refreshed: total={}", articles.size());
    }
}
