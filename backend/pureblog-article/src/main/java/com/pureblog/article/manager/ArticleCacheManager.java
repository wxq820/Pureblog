package com.pureblog.article.manager;

import com.pureblog.article.entity.ArticleDO;
import com.pureblog.article.vo.ArticleDetailVO;
import com.pureblog.common.constant.RedisKey;
import com.pureblog.common.enums.ArticleStatus;
import com.pureblog.common.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleCacheManager {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final long DETAIL_CACHE_TTL = 30;
    private static final long LIST_CACHE_TTL = 5;
    private static final long HOT_CACHE_TTL = 10;

    public void cacheArticleDetail(Long articleId, ArticleDetailVO vo) {
        String key = RedisKey.articleDetail(articleId);
        redisTemplate.opsForValue().set(key, JsonUtils.toJson(vo), DETAIL_CACHE_TTL, TimeUnit.MINUTES);
    }

    public ArticleDetailVO getArticleDetail(Long articleId) {
        String key = RedisKey.articleDetail(articleId);
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return JsonUtils.fromJson(cached.toString(), ArticleDetailVO.class);
        }
        return null;
    }

    public void evictArticleDetail(Long articleId) {
        redisTemplate.delete(RedisKey.articleDetail(articleId));
    }

    public void incrementPv(Long articleId) {
        String key = RedisKey.articlePv(articleId);
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 24, TimeUnit.HOURS);
    }

    public Long getPv(Long articleId) {
        String key = RedisKey.articlePv(articleId);
        Object val = redisTemplate.opsForValue().get(key);
        return val != null ? Long.parseLong(val.toString()) : 0L;
    }

    public void updateHotScore(Long articleId, double score) {
        String key = "pureblog:article:hot:zset";
        redisTemplate.opsForZSet().add(key, String.valueOf(articleId), score);
        redisTemplate.expire(key, HOT_CACHE_TTL, TimeUnit.MINUTES);
    }

    public List<Long> getHotArticleIds(int limit) {
        String key = "pureblog:article:hot:zset";
        Set<Object> range = redisTemplate.opsForZSet().reverseRange(key, 0, limit - 1);
        if (range == null || range.isEmpty()) {
            return Collections.emptyList();
        }
        return range.stream().map(o -> Long.parseLong(o.toString())).collect(Collectors.toList());
    }
}
