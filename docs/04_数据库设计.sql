-- =========================================================
-- PureBlog 数据库设计
-- MySQL 8.0
-- 字符集：utf8mb4
-- 说明：
-- 1. 本 SQL 为第一阶段可直接开发的基础版本
-- 2. 文章元数据与正文拆表，便于列表查询和正文独立维护
-- 3. 搜索依赖 Elasticsearch，不直接做复杂全文索引
-- 4. 所有表统一保留审计字段
-- =========================================================

CREATE DATABASE IF NOT EXISTS pureblog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pureblog;

SET NAMES utf8mb4;

-- ----------------------------
-- 用户表
-- ----------------------------
DROP TABLE IF EXISTS pb_user;
CREATE TABLE pb_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(64) NOT NULL COMMENT '用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    nickname VARCHAR(64) NOT NULL COMMENT '昵称',
    avatar_url VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    email VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    mobile VARCHAR(32) DEFAULT NULL COMMENT '手机号',
    bio VARCHAR(512) DEFAULT NULL COMMENT '个人简介',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1正常 0禁用',
    is_author TINYINT NOT NULL DEFAULT 0 COMMENT '是否作者',
    last_login_time DATETIME DEFAULT NULL COMMENT '最近登录时间',
    created_by BIGINT DEFAULT NULL COMMENT '创建人',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pb_user_username (username),
    UNIQUE KEY uk_pb_user_email (email),
    KEY idx_pb_user_status (status),
    KEY idx_pb_user_author (is_author)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 角色表
-- ----------------------------
DROP TABLE IF EXISTS pb_role;
CREATE TABLE pb_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    created_by BIGINT DEFAULT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT DEFAULT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pb_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

DROP TABLE IF EXISTS pb_user_role;
CREATE TABLE pb_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_by BIGINT DEFAULT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT DEFAULT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pb_user_role (user_id, role_id),
    KEY idx_pb_user_role_user (user_id),
    KEY idx_pb_user_role_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ----------------------------
-- 分类表
-- ----------------------------
DROP TABLE IF EXISTS pb_category;
CREATE TABLE pb_category (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(64) NOT NULL COMMENT '分类名称',
    slug VARCHAR(64) NOT NULL COMMENT '分类英文标识',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
    created_by BIGINT DEFAULT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT DEFAULT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pb_category_slug (slug),
    KEY idx_pb_category_status_sort (status, sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章分类表';

-- ----------------------------
-- 标签表
-- ----------------------------
DROP TABLE IF EXISTS pb_tag;
CREATE TABLE pb_tag (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(64) NOT NULL COMMENT '标签名称',
    slug VARCHAR(64) NOT NULL COMMENT '标签标识',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
    created_by BIGINT DEFAULT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT DEFAULT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pb_tag_slug (slug),
    KEY idx_pb_tag_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签表';

-- ----------------------------
-- 文章主表（元数据）
-- ----------------------------
DROP TABLE IF EXISTS pb_article;
CREATE TABLE pb_article (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    author_id BIGINT NOT NULL COMMENT '作者ID',
    category_id BIGINT DEFAULT NULL COMMENT '分类ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    summary VARCHAR(500) DEFAULT NULL COMMENT '摘要',
    cover_url VARCHAR(255) DEFAULT NULL COMMENT '封面图',
    slug VARCHAR(128) NOT NULL COMMENT 'URL 标识',
    status VARCHAR(32) NOT NULL COMMENT 'DRAFT/PENDING/PUBLISHED/OFFLINE/DELETED',
    visibility VARCHAR(32) NOT NULL DEFAULT 'PUBLIC' COMMENT 'PUBLIC/PRIVATE',
    publish_time DATETIME DEFAULT NULL COMMENT '发布时间',
    latest_version_no INT NOT NULL DEFAULT 1 COMMENT '最新版本号',
    allow_comment TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许评论',
    is_top TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶',
    deleted_time DATETIME DEFAULT NULL COMMENT '删除时间',
    created_by BIGINT DEFAULT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT DEFAULT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pb_article_slug (slug),
    KEY idx_pb_article_author (author_id),
    KEY idx_pb_article_category (category_id),
    KEY idx_pb_article_status_publish (status, publish_time),
    KEY idx_pb_article_top_publish (is_top, publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章主表';

-- ----------------------------
-- 文章正文表
-- ----------------------------
DROP TABLE IF EXISTS pb_article_content;
CREATE TABLE pb_article_content (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    article_id BIGINT NOT NULL COMMENT '文章ID',
    content_md LONGTEXT NOT NULL COMMENT 'Markdown 正文',
    content_html LONGTEXT DEFAULT NULL COMMENT '渲染后 HTML',
    word_count INT NOT NULL DEFAULT 0 COMMENT '字数',
    created_by BIGINT DEFAULT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT DEFAULT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pb_article_content_article (article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章正文表';

-- ----------------------------
-- 文章版本表
-- ----------------------------
DROP TABLE IF EXISTS pb_article_version;
CREATE TABLE pb_article_version (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    article_id BIGINT NOT NULL COMMENT '文章ID',
    version_no INT NOT NULL COMMENT '版本号',
    title VARCHAR(200) NOT NULL COMMENT '版本标题',
    summary VARCHAR(500) DEFAULT NULL COMMENT '版本摘要',
    content_md LONGTEXT NOT NULL COMMENT '版本正文',
    operator_id BIGINT NOT NULL COMMENT '操作人',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pb_article_version (article_id, version_no),
    KEY idx_pb_article_version_article (article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章版本表';

-- ----------------------------
-- 文章标签关联表
-- ----------------------------
DROP TABLE IF EXISTS pb_article_tag;
CREATE TABLE pb_article_tag (
    id BIGINT NOT NULL AUTO_INCREMENT,
    article_id BIGINT NOT NULL COMMENT '文章ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    created_by BIGINT DEFAULT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT DEFAULT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pb_article_tag (article_id, tag_id),
    KEY idx_pb_article_tag_tag (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签关联表';

-- ----------------------------
-- 评论表
-- ----------------------------
DROP TABLE IF EXISTS pb_comment;
CREATE TABLE pb_comment (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    article_id BIGINT NOT NULL COMMENT '文章ID',
    user_id BIGINT NOT NULL COMMENT '评论用户ID',
    root_id BIGINT DEFAULT NULL COMMENT '根评论ID',
    parent_id BIGINT DEFAULT NULL COMMENT '父评论ID',
    reply_user_id BIGINT DEFAULT NULL COMMENT '被回复用户ID',
    content VARCHAR(2000) NOT NULL COMMENT '评论内容',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/DELETED',
    like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    created_by BIGINT DEFAULT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT DEFAULT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_pb_comment_article_status (article_id, status, created_time),
    KEY idx_pb_comment_parent (parent_id),
    KEY idx_pb_comment_root (root_id),
    KEY idx_pb_comment_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- ----------------------------
-- 关注关系表
-- ----------------------------
DROP TABLE IF EXISTS pb_follow;
CREATE TABLE pb_follow (
    id BIGINT NOT NULL AUTO_INCREMENT,
    follower_id BIGINT NOT NULL COMMENT '关注者',
    followee_id BIGINT NOT NULL COMMENT '被关注作者',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1关注 0取消',
    created_by BIGINT DEFAULT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT DEFAULT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pb_follow (follower_id, followee_id),
    KEY idx_pb_follow_followee (followee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注关系表';

-- ----------------------------
-- 通知表
-- ----------------------------
DROP TABLE IF EXISTS pb_notification;
CREATE TABLE pb_notification (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    type VARCHAR(32) NOT NULL COMMENT 'ARTICLE_PUBLISH/COMMENT_REPLY/SYSTEM',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content VARCHAR(1000) DEFAULT NULL COMMENT '通知内容',
    biz_id BIGINT DEFAULT NULL COMMENT '业务ID',
    biz_type VARCHAR(32) DEFAULT NULL COMMENT '业务类型 ARTICLE/COMMENT',
    read_status TINYINT NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
    created_by BIGINT DEFAULT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT DEFAULT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_pb_notification_user_read (user_id, read_status, created_time),
    KEY idx_pb_notification_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- ----------------------------
-- 文章统计表（聚合）
-- ----------------------------
DROP TABLE IF EXISTS pb_article_stats;
CREATE TABLE pb_article_stats (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    article_id BIGINT NOT NULL COMMENT '文章ID',
    view_count BIGINT NOT NULL DEFAULT 0 COMMENT '浏览量',
    unique_view_count BIGINT NOT NULL DEFAULT 0 COMMENT '独立访客量',
    like_count BIGINT NOT NULL DEFAULT 0 COMMENT '点赞数',
    favorite_count BIGINT NOT NULL DEFAULT 0 COMMENT '收藏数',
    comment_count BIGINT NOT NULL DEFAULT 0 COMMENT '评论数',
    score DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '热度分',
    last_calc_time DATETIME DEFAULT NULL COMMENT '最近热度计算时间',
    created_by BIGINT DEFAULT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT DEFAULT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pb_article_stats_article (article_id),
    KEY idx_pb_article_stats_score (score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章统计聚合表';

-- ----------------------------
-- 文章日统计表
-- ----------------------------
DROP TABLE IF EXISTS pb_article_stats_daily;
CREATE TABLE pb_article_stats_daily (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    article_id BIGINT NOT NULL COMMENT '文章ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    view_count BIGINT NOT NULL DEFAULT 0 COMMENT '当天浏览量',
    unique_view_count BIGINT NOT NULL DEFAULT 0 COMMENT '当天独立访客量',
    like_count BIGINT NOT NULL DEFAULT 0 COMMENT '当天点赞数',
    comment_count BIGINT NOT NULL DEFAULT 0 COMMENT '当天评论数',
    created_by BIGINT DEFAULT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT DEFAULT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pb_article_stats_daily (article_id, stat_date),
    KEY idx_pb_article_stats_daily_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章日统计表';

-- ----------------------------
-- Outbox 事件表（建议第二阶段启用）
-- ----------------------------
DROP TABLE IF EXISTS pb_outbox_event;
CREATE TABLE pb_outbox_event (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    event_key VARCHAR(64) NOT NULL COMMENT '事件唯一键',
    event_type VARCHAR(64) NOT NULL COMMENT '事件类型',
    aggregate_type VARCHAR(64) NOT NULL COMMENT '聚合类型',
    aggregate_id BIGINT NOT NULL COMMENT '聚合ID',
    payload_json JSON NOT NULL COMMENT '事件内容',
    status VARCHAR(32) NOT NULL DEFAULT 'NEW' COMMENT 'NEW/SENT/FAILED',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    next_retry_time DATETIME DEFAULT NULL COMMENT '下次重试时间',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pb_outbox_event_key (event_key),
    KEY idx_pb_outbox_status_retry (status, next_retry_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Outbox 事件表';

-- ----------------------------
-- 消费幂等记录表
-- ----------------------------
DROP TABLE IF EXISTS pb_message_consume_record;
CREATE TABLE pb_message_consume_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    message_key VARCHAR(64) NOT NULL COMMENT '消息唯一键',
    consumer_group_name VARCHAR(128) NOT NULL COMMENT '消费组',
    topic_name VARCHAR(128) NOT NULL COMMENT 'Topic',
    consume_status VARCHAR(32) NOT NULL COMMENT 'SUCCESS/FAILED',
    consumed_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消费时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pb_msg_consume (message_key, consumer_group_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息消费幂等记录表';

-- ----------------------------
-- 初始化角色
-- ----------------------------
INSERT INTO pb_role (role_code, role_name, status, remark) VALUES
('SUPER_ADMIN', '超级管理员', 1, '系统最高权限'),
('ADMIN', '管理员', 1, '运营管理权限'),
('AUTHOR', '作者', 1, '作者权限'),
('USER', '普通用户', 1, '普通用户权限');
