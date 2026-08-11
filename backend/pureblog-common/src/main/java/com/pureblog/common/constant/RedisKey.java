package com.pureblog.common.constant;

public class RedisKey {

    private static final String PREFIX = "pureblog:";

    public static String articleDetail(Long articleId) {
        return PREFIX + "article:detail:" + articleId;
    }

    public static String articleList(String key) {
        return PREFIX + "article:list:" + key;
    }

    public static String articleHotTop(int limit) {
        return PREFIX + "article:hot:top" + limit;
    }

    public static String articleHotCategory(Long categoryId, int limit) {
        return PREFIX + "article:hot:category:" + categoryId + ":" + limit;
    }

    public static String articlePv(Long articleId) {
        return PREFIX + "article:pv:" + articleId;
    }

    public static String userInfo(Long userId) {
        return PREFIX + "user:info:" + userId;
    }

    public static String loginCaptcha(String uuid) {
        return PREFIX + "captcha:login:" + uuid;
    }

    public static String rateLimit(String type, Long userId) {
        return PREFIX + "rate:" + type + ":" + userId;
    }

    public static String tokenBlacklist(String token) {
        return PREFIX + "token:blacklist:" + token;
    }

    public static String refreshToken(Long userId) {
        return PREFIX + "refresh:token:" + userId;
    }

    public static String outboxScan() {
        return PREFIX + "outbox:scan:lock";
    }

    private RedisKey() {}
}
