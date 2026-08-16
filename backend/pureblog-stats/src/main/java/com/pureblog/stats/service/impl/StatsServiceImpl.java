package com.pureblog.stats.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pureblog.article.entity.ArticleDO;
import com.pureblog.article.mapper.ArticleMapper;
import com.pureblog.auth.mapper.UserMapper;
import com.pureblog.common.enums.ArticleStatus;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PV_KEY_PREFIX = "pureblog:article:pv:";
    private static final String UV_KEY_PREFIX = "pureblog:article:uv:";
    private static final String HOT_ZSET_KEY = "pureblog:article:hot:zset";
    private static final int LIKE_HOT_WEIGHT = 10;

    @Override
    public void incrementPv(Long articleId, String ip) {
        String pvKey = PV_KEY_PREFIX + articleId;
        redisTemplate.opsForValue().increment(pvKey);
        redisTemplate.expire(pvKey, 2, TimeUnit.DAYS);

        if (ip != null) {
            String uvKey = UV_KEY_PREFIX + articleId + ":" + LocalDate.now();
            redisTemplate.opsForSet().add(uvKey, ip);
            redisTemplate.expire(uvKey, 2, TimeUnit.DAYS);
        }

        addHotScore(articleId, 1);
    }

    @Override
    public void recordLikeHotScore(Long articleId) {
        addHotScore(articleId, LIKE_HOT_WEIGHT);
    }

    private void addHotScore(Long articleId, int delta) {
        Double currentScore = redisTemplate.opsForZSet().score(HOT_ZSET_KEY, String.valueOf(articleId));
        double newScore = (currentScore != null ? currentScore : 0) + delta;
        redisTemplate.opsForZSet().add(HOT_ZSET_KEY, String.valueOf(articleId), newScore);
        redisTemplate.expire(HOT_ZSET_KEY, 7, TimeUnit.DAYS);
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
        Set<ZSetOperations.TypedTuple<Object>> topSet = redisTemplate.opsForZSet()
                .reverseRangeWithScores(HOT_ZSET_KEY, 0, limit - 1);

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

        redisTemplate.delete(HOT_ZSET_KEY);

        for (ArticleDO article : articles) {
            int score = article.getViewCount() + article.getLikeCount() * 10 + article.getCommentCount() * 5;
            redisTemplate.opsForZSet().add(HOT_ZSET_KEY, String.valueOf(article.getId()), score);
        }

        redisTemplate.expire(HOT_ZSET_KEY, 7, TimeUnit.DAYS);
        log.info("Hot articles refreshed: total={}", articles.size());
    }
}