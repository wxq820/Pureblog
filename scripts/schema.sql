-- ============================================================
-- PureBlog Database Schema
-- MySQL 8.0
-- ============================================================

CREATE DATABASE IF NOT EXISTS pureblog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pureblog;

-- -------------------------------------------------------
-- 1. pb_user 用户表
-- -------------------------------------------------------
CREATE TABLE pb_user (
    id              BIGINT UNSIGNED    NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
    username        VARCHAR(50)       NOT NULL                COMMENT '用户名',
    email           VARCHAR(255)      NOT NULL                COMMENT '邮箱',
    password_hash   VARCHAR(255)      NOT NULL                COMMENT '密码哈希',
    nickname        VARCHAR(100)      DEFAULT NULL            COMMENT '昵称',
    avatar_url      VARCHAR(500)      DEFAULT NULL            COMMENT '头像URL',
    bio             VARCHAR(500)      DEFAULT NULL            COMMENT '个人简介',
    role            TINYINT UNSIGNED  NOT NULL DEFAULT 1       COMMENT '角色: 1-普通用户 2-作者 3-管理员 4-超管',
    status          TINYINT UNSIGNED  NOT NULL DEFAULT 1       COMMENT '状态: 1-正常 2-禁用',
    follower_count  INT UNSIGNED       NOT NULL DEFAULT 0      COMMENT '粉丝数',
    following_count INT UNSIGNED       NOT NULL DEFAULT 0      COMMENT '关注数',
    article_count   INT UNSIGNED       NOT NULL DEFAULT 0      COMMENT '文章数',
    last_login_at   DATETIME          DEFAULT NULL            COMMENT '最后登录时间',
    created_at      DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT UNSIGNED  NOT NULL DEFAULT 0      COMMENT '软删除: 0-否 1-是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- -------------------------------------------------------
-- 2. pb_category 分类表
-- -------------------------------------------------------
CREATE TABLE pb_category (
    id          BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '分类ID',
    name        VARCHAR(50)       NOT NULL                COMMENT '分类名称',
    slug        VARCHAR(50)       NOT NULL                COMMENT '分类别名',
    description VARCHAR(255)      DEFAULT NULL            COMMENT '分类描述',
    sort_order  INT UNSIGNED      NOT NULL DEFAULT 0      COMMENT '排序',
    article_count INT UNSIGNED     NOT NULL DEFAULT 0      COMMENT '文章数',
    created_at  DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT UNSIGNED   NOT NULL DEFAULT 0      COMMENT '软删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- -------------------------------------------------------
-- 3. pb_tag 标签表
-- -------------------------------------------------------
CREATE TABLE pb_tag (
    id           BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '标签ID',
    name         VARCHAR(50)      NOT NULL                 COMMENT '标签名称',
    slug         VARCHAR(50)      NOT NULL                 COMMENT '标签别名',
    article_count INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '文章数',
    created_at   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT UNSIGNED  NOT NULL DEFAULT 0      COMMENT '软删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name),
    UNIQUE KEY uk_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- -------------------------------------------------------
-- 4. pb_article 文章元数据表
-- -------------------------------------------------------
CREATE TABLE pb_article (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '文章ID',
    author_id       BIGINT UNSIGNED   NOT NULL                 COMMENT '作者ID',
    category_id     BIGINT UNSIGNED   DEFAULT NULL             COMMENT '分类ID',
    title           VARCHAR(255)      NOT NULL                 COMMENT '文章标题',
    summary         VARCHAR(500)      DEFAULT NULL             COMMENT '文章摘要',
    cover_url       VARCHAR(500)      DEFAULT NULL             COMMENT '封面图URL',
    status          TINYINT UNSIGNED  NOT NULL DEFAULT 0       COMMENT '状态: 0-草稿 1-已发布 2-已下架',
    view_count      INT UNSIGNED      NOT NULL DEFAULT 0       COMMENT '浏览量',
    like_count      INT UNSIGNED      NOT NULL DEFAULT 0       COMMENT '点赞数',
    comment_count   INT UNSIGNED      NOT NULL DEFAULT 0       COMMENT '评论数',
    collect_count   INT UNSIGNED      NOT NULL DEFAULT 0       COMMENT '收藏数',
    is_featured     TINYINT UNSIGNED  NOT NULL DEFAULT 0       COMMENT '是否精选',
    is_top          TINYINT UNSIGNED  NOT NULL DEFAULT 0       COMMENT '是否置顶',
    published_at    DATETIME          DEFAULT NULL             COMMENT '发布时间',
    created_at      DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT UNSIGNED   NOT NULL DEFAULT 0       COMMENT '软删除',
    PRIMARY KEY (id),
    KEY idx_author_id (author_id),
    KEY idx_category_id (category_id),
    KEY idx_status (status),
    KEY idx_published_at (published_at),
    KEY idx_is_featured (is_featured),
    KEY idx_is_top (is_top)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章元数据表';

-- -------------------------------------------------------
-- 5. pb_article_content 文章正文表
-- -------------------------------------------------------
CREATE TABLE pb_article_content (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '内容ID',
    article_id  BIGINT UNSIGNED  NOT NULL                COMMENT '文章ID',
    content     LONGTEXT         NOT NULL                COMMENT 'Markdown正文',
    html_content LONGTEXT        DEFAULT NULL             COMMENT '渲染后HTML',
    word_count  INT UNSIGNED     NOT NULL DEFAULT 0      COMMENT '字数',
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_article_id (article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章正文表';

-- -------------------------------------------------------
-- 6. pb_article_tag 文章标签关联表
-- -------------------------------------------------------
CREATE TABLE pb_article_tag (
    id         BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    article_id BIGINT UNSIGNED  NOT NULL                 COMMENT '文章ID',
    tag_id     BIGINT UNSIGNED  NOT NULL                 COMMENT '标签ID',
    created_at DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_article_tag (article_id, tag_id),
    KEY idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签关联表';

-- -------------------------------------------------------
-- 7. pb_comment 评论表
-- -------------------------------------------------------
CREATE TABLE pb_comment (
    id           BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '评论ID',
    article_id   BIGINT UNSIGNED  NOT NULL                 COMMENT '文章ID',
    user_id      BIGINT UNSIGNED  NOT NULL                 COMMENT '评论用户ID',
    parent_id    BIGINT UNSIGNED  DEFAULT NULL             COMMENT '父评论ID(0表示顶级)',
    reply_to_id  BIGINT UNSIGNED  DEFAULT NULL             COMMENT '回复目标评论ID',
    reply_to_uid BIGINT UNSIGNED  DEFAULT NULL             COMMENT '回复目标用户ID',
    content      VARCHAR(1000)    NOT NULL                 COMMENT '评论内容',
    like_count   INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '点赞数',
    status       TINYINT UNSIGNED  NOT NULL DEFAULT 0       COMMENT '状态: 0-待审核 1-通过 2-拒绝',
    created_at   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT UNSIGNED  NOT NULL DEFAULT 0       COMMENT '软删除',
    PRIMARY KEY (id),
    KEY idx_article_id (article_id),
    KEY idx_user_id (user_id),
    KEY idx_parent_id (parent_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- -------------------------------------------------------
-- 8. pb_follow 关注关系表
-- -------------------------------------------------------
CREATE TABLE pb_follow (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    follower_id BIGINT UNSIGNED  NOT NULL                 COMMENT '粉丝ID',
    following_id BIGINT UNSIGNED NOT NULL                 COMMENT '关注用户ID',
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_follow (follower_id, following_id),
    KEY idx_following_id (following_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关注关系表';

-- -------------------------------------------------------
-- 9. pb_collect 收藏表
-- -------------------------------------------------------
CREATE TABLE pb_collect (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    user_id     BIGINT UNSIGNED  NOT NULL                 COMMENT '用户ID',
    article_id  BIGINT UNSIGNED  NOT NULL                 COMMENT '文章ID',
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_collect (user_id, article_id),
    KEY idx_article_id (article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- -------------------------------------------------------
-- 10. pb_like 点赞表(文章/评论点赞共用)
-- -------------------------------------------------------
CREATE TABLE pb_like (
    id         BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    user_id    BIGINT UNSIGNED  NOT NULL                 COMMENT '用户ID',
    target_id  BIGINT UNSIGNED  NOT NULL                 COMMENT '目标ID',
    target_type TINYINT UNSIGNED NOT NULL                COMMENT '类型: 1-文章 2-评论',
    created_at DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_like (user_id, target_id, target_type),
    KEY idx_target (target_id, target_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞表';

-- -------------------------------------------------------
-- 11. pb_notification 通知表
-- -------------------------------------------------------
CREATE TABLE pb_notification (
    id           BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '通知ID',
    user_id      BIGINT UNSIGNED  NOT NULL                 COMMENT '接收用户ID',
    type         TINYINT UNSIGNED NOT NULL                 COMMENT '类型: 1-评论 2-回复 3-关注 4-点赞 5-收藏 6-系统通知',
    title        VARCHAR(255)     DEFAULT NULL             COMMENT '通知标题',
    content      VARCHAR(1000)   DEFAULT NULL             COMMENT '通知内容',
    related_id   BIGINT UNSIGNED  DEFAULT NULL             COMMENT '关联ID(文章ID/评论ID等)',
    related_type TINYINT UNSIGNED DEFAULT NULL             COMMENT '关联类型',
    is_read      TINYINT UNSIGNED NOT NULL DEFAULT 0       COMMENT '是否已读: 0-否 1-是',
    created_at   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_is_read (is_read),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- -------------------------------------------------------
-- 12. pb_login_log 登录日志表
-- -------------------------------------------------------
CREATE TABLE pb_login_log (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    user_id     BIGINT UNSIGNED  DEFAULT NULL             COMMENT '用户ID',
    username    VARCHAR(50)      DEFAULT NULL             COMMENT '用户名',
    ip          VARCHAR(50)      DEFAULT NULL             COMMENT '登录IP',
    user_agent  VARCHAR(500)     DEFAULT NULL             COMMENT 'User-Agent',
    status      TINYINT UNSIGNED NOT NULL DEFAULT 1       COMMENT '状态: 1-成功 2-失败',
    msg         VARCHAR(255)     DEFAULT NULL             COMMENT '备注信息',
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- -------------------------------------------------------
-- 13. pb_kafka_outbox Kafka Outbox表(保证消息可靠投递)
-- -------------------------------------------------------
CREATE TABLE pb_kafka_outbox (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    topic       VARCHAR(100)     NOT NULL                 COMMENT 'Kafka Topic',
    key         VARCHAR(255)     DEFAULT NULL             COMMENT '消息Key',
    payload     TEXT             NOT NULL                 COMMENT '消息内容(JSON)',
    status      TINYINT UNSIGNED NOT NULL DEFAULT 0       COMMENT '状态: 0-待发送 1-已发送 2-失败',
    retry_count INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '重试次数',
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    sent_at     DATETIME         DEFAULT NULL             COMMENT '发送时间',
    PRIMARY KEY (id),
    KEY idx_status (status),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Kafka Outbox表';

-- -------------------------------------------------------
-- 14. pb_article_stats 文章统计表(定时聚合)
-- -------------------------------------------------------
CREATE TABLE pb_article_stats (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    article_id  BIGINT UNSIGNED  NOT NULL                 COMMENT '文章ID',
    pv          INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '页面浏览量',
    uv          INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '独立访客数',
    date        DATE             NOT NULL                 COMMENT '统计日期',
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_article_date (article_id, date),
    KEY idx_date (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章统计表';

-- -------------------------------------------------------
-- 15. pb_file 文件表
-- -------------------------------------------------------
CREATE TABLE pb_file (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    uploader_id BIGINT UNSIGNED  NOT NULL                 COMMENT '上传者ID',
    file_name   VARCHAR(255)     NOT NULL                 COMMENT '文件名',
    file_key    VARCHAR(255)     NOT NULL                 COMMENT 'MinIO存储Key',
    file_url    VARCHAR(500)     NOT NULL                 COMMENT '访问URL',
    file_size   BIGINT UNSIGNED  NOT NULL                 COMMENT '文件大小(字节)',
    mime_type   VARCHAR(100)     DEFAULT NULL             COMMENT 'MIME类型',
    file_type   TINYINT UNSIGNED NOT NULL                 COMMENT '文件类型: 1-图片 2-视频 3-文档 4-其他',
    bucket      VARCHAR(50)      DEFAULT NULL             COMMENT '存储桶',
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (id),
    KEY idx_uploader_id (uploader_id),
    KEY idx_file_type (file_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件表';

-- -------------------------------------------------------
-- 16. pb_system_config 系统配置表
-- -------------------------------------------------------
CREATE TABLE pb_system_config (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    config_key  VARCHAR(100)     NOT NULL                 COMMENT '配置键',
    config_value TEXT            DEFAULT NULL             COMMENT '配置值',
    description VARCHAR(255)     DEFAULT NULL             COMMENT '描述',
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- -------------------------------------------------------
-- 初始数据
-- -------------------------------------------------------

-- 插入超管账户 (密码: admin123, BCrypt加密)
INSERT INTO pb_user (username, email, password_hash, nickname, role, status) VALUES
('admin', 'admin@pureblog.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '管理员', 4, 1);

-- 插入默认分类
INSERT INTO pb_category (name, slug, description, sort_order) VALUES
('技术', 'tech', '技术相关文章', 1),
('生活', 'life', '生活随笔', 2),
('读书', 'reading', '读书笔记', 3),
('随想', 'thoughts', '随想杂谈', 4);

-- 插入默认标签
INSERT INTO pb_tag (name, slug) VALUES
('Java', 'java'),
('Spring Boot', 'spring-boot'),
('Redis', 'redis'),
('MySQL', 'mysql'),
('Elasticsearch', 'elasticsearch'),
('Kafka', 'kafka'),
('Docker', 'docker'),
('Vue', 'vue'),
('架构设计', 'architecture'),
('性能优化', 'performance');

-- 插入默认系统配置
INSERT INTO pb_system_config (config_key, config_value, description) VALUES
('site_name', 'PureBlog', '网站名称'),
('site_description', '纯粹的技术博客平台', '网站描述'),
('article_per_page', '10', '每页文章数'),
('hot_article_days', '7', '热榜统计天数'),
('comment_need_audit', 'true', '评论是否需要审核'),
('upload_max_size', '10485760', '文件上传大小限制(字节)');
