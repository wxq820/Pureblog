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
    tree_id         BIGINT UNSIGNED   DEFAULT NULL             COMMENT '目录树主表ID',
    tree_node_id    BIGINT UNSIGNED   DEFAULT NULL             COMMENT '目录树叶子节点ID',
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
    KEY idx_is_top (is_top),
    KEY idx_pb_article_tree_node (tree_node_id),
    KEY idx_pb_article_tree (tree_id)
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
    msg_key     VARCHAR(255)     DEFAULT NULL             COMMENT '消息Key',
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
-- 17. pb_tree 目录树主表
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS pb_tree (
    id           BIGINT       NOT NULL PRIMARY KEY,
    code         VARCHAR(64)  NOT NULL,
    name         VARCHAR(64)  NOT NULL,
    description  VARCHAR(255) DEFAULT NULL,
    cover_color  VARCHAR(16)  DEFAULT '#2563eb',
    sort_order   INT          NOT NULL DEFAULT 0,
    status       TINYINT      NOT NULL DEFAULT 1 COMMENT '1:启用 0:禁用',
    created_at   DATETIME     DEFAULT NULL,
    updated_at   DATETIME     DEFAULT NULL,
    deleted      TINYINT      NOT NULL DEFAULT 0,
    UNIQUE KEY uk_pb_tree_code (code),
    KEY idx_pb_tree_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='目录树主表';

-- -------------------------------------------------------
-- 18. pb_tree_node 目录树节点表
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS pb_tree_node (
    id            BIGINT       NOT NULL PRIMARY KEY,
    tree_id       BIGINT       NOT NULL COMMENT '所属目录树',
    parent_id     BIGINT       DEFAULT NULL COMMENT '父节点 NULL=根',
    name          VARCHAR(128) NOT NULL,
    color         VARCHAR(16)  DEFAULT NULL,
    sort_order    INT          NOT NULL DEFAULT 0,
    depth         INT          NOT NULL DEFAULT 0 COMMENT '0=根,叶节点通常 depth>=1',
    path          VARCHAR(512) DEFAULT NULL COMMENT '物化路径 /id1/id2/id3/',
    article_count INT          NOT NULL DEFAULT 0,
    created_at    DATETIME     DEFAULT NULL,
    updated_at    DATETIME     DEFAULT NULL,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    KEY idx_pb_tree_node_tree_parent (tree_id, parent_id),
    KEY idx_pb_tree_node_tree_path   (tree_id, path)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='目录树节点';

-- -------------------------------------------------------
-- 19. pb_widget 小工具主表
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS pb_widget (
    id           BIGINT       NOT NULL PRIMARY KEY,
    code         VARCHAR(64)  NOT NULL,
    name         VARCHAR(64)  NOT NULL,
    description  VARCHAR(255) DEFAULT NULL,
    icon         VARCHAR(32)  DEFAULT NULL COMMENT 'Element Plus Icons 图标名, 如 EditPen/Wallet',
    icon_color   VARCHAR(16)  DEFAULT '#2563eb',
    path         VARCHAR(128) NOT NULL COMMENT '前端路径',
    sort_order   INT          NOT NULL DEFAULT 0,
    status       TINYINT      NOT NULL DEFAULT 1 COMMENT '1:启用 0:禁用',
    click_count  INT          NOT NULL DEFAULT 0 COMMENT '总点击数 (冗余, 用于排序)',
    created_at   DATETIME     DEFAULT NULL,
    updated_at   DATETIME     DEFAULT NULL,
    deleted      TINYINT      NOT NULL DEFAULT 0,
    UNIQUE KEY uk_pb_widget_code (code),
    KEY idx_pb_widget_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小工具主表';

-- -------------------------------------------------------
-- 20. pb_widget_click 小工具点击流水
-- -------------------------------------------------------
-- UNIQUE KEY (widget_id, device_id, click_date) 天然去重:
-- 同设备同一天对同一个小工具只计 1 次
CREATE TABLE IF NOT EXISTS pb_widget_click (
    id          BIGINT       NOT NULL PRIMARY KEY,
    widget_id   BIGINT       NOT NULL,
    device_id   VARCHAR(64)  NOT NULL,
    click_date  DATE         NOT NULL,
    created_at  DATETIME     DEFAULT NULL,
    UNIQUE KEY uk_pb_widget_click_dedup (widget_id, device_id, click_date),
    KEY idx_pb_widget_click_widget (widget_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小工具点击流水 (按设备+日期去重)';

-- -------------------------------------------------------
-- 21. pb_ledger_dict 记账字典表 (平台 + 项目合一, type 区分)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS pb_ledger_dict (
    id           BIGINT       NOT NULL PRIMARY KEY,
    type         TINYINT      NOT NULL COMMENT '1:平台 2:项目',
    name         VARCHAR(64)  NOT NULL,
    color        VARCHAR(16)  DEFAULT '#6b7280',
    icon         VARCHAR(32)  DEFAULT NULL,
    sort_order   INT          NOT NULL DEFAULT 0,
    status       TINYINT      NOT NULL DEFAULT 1,
    created_at   DATETIME     DEFAULT NULL,
    updated_at   DATETIME     DEFAULT NULL,
    deleted      TINYINT      NOT NULL DEFAULT 0,
    KEY idx_pb_ledger_dict_type_status (type, status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='记账字典:平台+项目';

-- -------------------------------------------------------
-- 22. pb_ledger_record 记账主表
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS pb_ledger_record (
    id                BIGINT         NOT NULL PRIMARY KEY,
    record_date       DATE           NOT NULL,
    platform_dict_id  BIGINT         DEFAULT NULL,
    item_dict_id      BIGINT         DEFAULT NULL COMMENT 'NULL=手动输入',
    item_text         VARCHAR(128)   DEFAULT NULL COMMENT '手动输入的项目文本,与 item_dict_id 互斥',
    amount            DECIMAL(10,2)  NOT NULL,
    device_id         VARCHAR(64)    NOT NULL,
    created_at        DATETIME       DEFAULT NULL,
    updated_at        DATETIME       DEFAULT NULL,
    deleted           TINYINT        NOT NULL DEFAULT 0,
    KEY idx_pb_ledger_record_date (record_date),
    KEY idx_pb_ledger_record_device (device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='记账主表';

-- -------------------------------------------------------
-- 23. pb_account_category 身家-账户分类/维度表
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS pb_account_category (
    id                   BIGINT       NOT NULL PRIMARY KEY,
    name                 VARCHAR(32)  NOT NULL,
    sort_order           INT          NOT NULL DEFAULT 0,
    is_counted_in_total  TINYINT      NOT NULL DEFAULT 1 COMMENT '是否计入"目前完整身家"',
    status               TINYINT      NOT NULL DEFAULT 1 COMMENT '1:启用 0:禁用',
    created_at           DATETIME     DEFAULT NULL,
    updated_at           DATETIME     DEFAULT NULL,
    deleted              TINYINT      NOT NULL DEFAULT 0,
    KEY idx_pb_account_cat_status (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='身家:账户分类/维度';

-- -------------------------------------------------------
-- 24. pb_account 身家-具体账户表
-- -------------------------------------------------------
-- 无 category 字段, 分类跟随 entry
CREATE TABLE IF NOT EXISTS pb_account (
    id          BIGINT       NOT NULL PRIMARY KEY,
    name        VARCHAR(64)  NOT NULL,
    icon        VARCHAR(32)  DEFAULT NULL,
    color       VARCHAR(16)  DEFAULT NULL,
    sort_order  INT          NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 1,
    created_at  DATETIME     DEFAULT NULL,
    updated_at  DATETIME     DEFAULT NULL,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    KEY idx_pb_account_status (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='身家:具体账户';

-- -------------------------------------------------------
-- 25. pb_balance_snapshot 身家-快照头表
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS pb_balance_snapshot (
    id           BIGINT       NOT NULL PRIMARY KEY,
    snapshot_at  DATETIME     NOT NULL COMMENT '快照日期 (按时间排序+算 delta)',
    event_tag    VARCHAR(32)  DEFAULT NULL COMMENT '事件标签:发薪/20号/月底...',
    remark       VARCHAR(255) DEFAULT NULL,
    created_at   DATETIME     DEFAULT NULL,
    deleted      TINYINT      NOT NULL DEFAULT 0,
    KEY idx_pb_balance_snapshot_at (snapshot_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='身家:快照头';

-- -------------------------------------------------------
-- 26. pb_balance_entry 身家-快照明细表
-- -------------------------------------------------------
-- 每行 = 一个账户在某个快照下的余额+当时的分类
CREATE TABLE IF NOT EXISTS pb_balance_entry (
    id           BIGINT         NOT NULL PRIMARY KEY,
    snapshot_id  BIGINT         NOT NULL,
    account_id   BIGINT         NOT NULL,
    category_id  BIGINT         NOT NULL,
    balance      DECIMAL(14,2)  NOT NULL DEFAULT 0,
    UNIQUE KEY uk_pb_balance_entry_snap_acct (snapshot_id, account_id),
    KEY idx_pb_balance_entry_snap_cat (snapshot_id, category_id),
    KEY idx_pb_balance_entry_acct (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='身家:快照明细 (per-snapshot category)';

-- -------------------------------------------------------
-- 27. pb_position 回本之路-仓位主表
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS pb_position (
    id          BIGINT         NOT NULL PRIMARY KEY,
    name        VARCHAR(64)    NOT NULL,
    cost        DECIMAL(14,2)  NOT NULL DEFAULT 0 COMMENT '初始成本',
    remark      VARCHAR(255)   DEFAULT NULL,
    sort_order  INT            NOT NULL DEFAULT 0,
    status      TINYINT        NOT NULL DEFAULT 1,
    created_at  DATETIME       DEFAULT NULL,
    updated_at  DATETIME       DEFAULT NULL,
    deleted     TINYINT        NOT NULL DEFAULT 0,
    KEY idx_pb_position_status (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回本之路:仓位';

-- -------------------------------------------------------
-- 28. pb_position_value 回本之路-仓位市值流水表
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS pb_position_value (
    id            BIGINT         NOT NULL PRIMARY KEY,
    position_id   BIGINT         NOT NULL,
    market_value  DECIMAL(14,2)  NOT NULL DEFAULT 0,
    recorded_at   DATETIME       NOT NULL,
    remark        VARCHAR(255)   DEFAULT NULL,
    created_at    DATETIME       DEFAULT NULL,
    deleted       TINYINT        NOT NULL DEFAULT 0,
    KEY idx_pb_position_value_pos_at (position_id, recorded_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回本之路:仓位市值流水';

-- -------------------------------------------------------
-- 29. pb_fund 投资表-基金/股票主表
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS pb_fund (
    id          BIGINT       NOT NULL PRIMARY KEY,
    name        VARCHAR(64)  NOT NULL,
    code        VARCHAR(32)  DEFAULT NULL COMMENT '基金代码/股票代码',
    type        VARCHAR(16)  DEFAULT NULL COMMENT '基金/股票/ETF/...',
    remark      VARCHAR(255) DEFAULT NULL,
    sort_order  INT          NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 1,
    created_at  DATETIME     DEFAULT NULL,
    updated_at  DATETIME     DEFAULT NULL,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    KEY idx_pb_fund_status (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投资表:基金/股票主表';

-- -------------------------------------------------------
-- 30. pb_fund_buy 投资表-买入记录表
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS pb_fund_buy (
    id         BIGINT         NOT NULL PRIMARY KEY,
    fund_id    BIGINT         NOT NULL,
    buy_date   DATE           NOT NULL,
    price      DECIMAL(10,4)  NOT NULL COMMENT '买入单价',
    shares     DECIMAL(14,2)  NOT NULL COMMENT '买入份额',
    amount     DECIMAL(14,2)  NOT NULL COMMENT '买入金额 (= price * shares)',
    remark     VARCHAR(255)   DEFAULT NULL,
    created_at DATETIME       DEFAULT NULL,
    updated_at DATETIME       DEFAULT NULL,
    deleted    TINYINT        NOT NULL DEFAULT 0,
    KEY idx_pb_fund_buy_fund (fund_id, buy_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投资表:买入记录';

-- -------------------------------------------------------
-- 31. pb_fund_nav 投资表-净值/价格流水表
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS pb_fund_nav (
    id          BIGINT         NOT NULL PRIMARY KEY,
    fund_id     BIGINT         NOT NULL,
    nav         DECIMAL(10,4)  NOT NULL COMMENT '当前净值/单价',
    recorded_at DATETIME       NOT NULL,
    remark      VARCHAR(255)   DEFAULT NULL,
    created_at  DATETIME       DEFAULT NULL,
    deleted     TINYINT        NOT NULL DEFAULT 0,
    KEY idx_pb_fund_nav_fund_at (fund_id, recorded_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投资表:净值流水';

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

-- -------------------------------------------------------
-- 初始数据 - 目录树 (pb_tree / pb_tree_node)
-- -------------------------------------------------------
INSERT INTO pb_tree (id, code, name, description, cover_color, sort_order, status, created_at, updated_at, deleted)
VALUES (1, 'java', 'Java 技术栈', 'Java 后端技术生态汇总', '#2563eb', 0, 1, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name), updated_at = NOW();

-- 节点采用手动指定 ID (便于维护);depth = 0 是根,depth = 4 是叶子的上一级 (java-se 区域).
INSERT INTO pb_tree_node (id, tree_id, parent_id, name, color, sort_order, depth, path, article_count, created_at, updated_at, deleted)
VALUES
    (1, 1, NULL, 'Java', '#2563eb', 0, 0, '/1/',         0, NOW(), NOW(), 0),
    -- Java SE
    (2, 1, 1,    'Java SE', '#3b82f6', 0, 1, '/1/2/',     0, NOW(), NOW(), 0),
    (3, 1, 2,    '集合框架', '#60a5fa', 0, 2, '/1/2/3/',   0, NOW(), NOW(), 0),
    (4, 1, 3,    'List', '#93c5fd', 0, 3, '/1/2/3/4/',   0, NOW(), NOW(), 0),
    (5, 1, 3,    'Map', '#93c5fd', 1, 3, '/1/2/3/5/',    0, NOW(), NOW(), 0),
    (6, 1, 3,    'Set', '#93c5fd', 2, 3, '/1/2/3/6/',    0, NOW(), NOW(), 0),
    (7, 1, 2,    'Stream API', '#60a5fa', 1, 2, '/1/2/7/', 0, NOW(), NOW(), 0),
    (8, 1, 2,    'Lambda 表达式', '#60a5fa', 2, 2, '/1/2/8/', 0, NOW(), NOW(), 0),
    (9, 1, 2,    '反射与注解', '#60a5fa', 3, 2, '/1/2/9/', 0, NOW(), NOW(), 0),
    (10, 1, 2,   'IO/NIO', '#60a5fa', 4, 2, '/1/2/10/', 0, NOW(), NOW(), 0),
    -- 并发编程
    (20, 1, 1,   '并发编程', '#10b981', 1, 1, '/1/20/',   0, NOW(), NOW(), 0),
    (21, 1, 20,  'JUC 并发包', '#34d399', 0, 2, '/1/20/21/', 0, NOW(), NOW(), 0),
    (22, 1, 20,  '线程池', '#34d399', 1, 2, '/1/20/22/', 0, NOW(), NOW(), 0),
    (23, 1, 20,  '锁机制', '#34d399', 2, 2, '/1/20/23/', 0, NOW(), NOW(), 0),
    (24, 1, 20,  '内存模型 JMM', '#34d399', 3, 2, '/1/20/24/', 0, NOW(), NOW(), 0),
    (25, 1, 20,  '线程通信', '#34d399', 4, 2, '/1/20/25/', 0, NOW(), NOW(), 0),
    -- JVM
    (30, 1, 1,   'JVM', '#8b5cf6', 2, 1, '/1/30/',      0, NOW(), NOW(), 0),
    (31, 1, 30,  '类加载机制', '#a78bfa', 0, 2, '/1/30/31/', 0, NOW(), NOW(), 0),
    (32, 1, 30,  '垃圾回收 GC', '#a78bfa', 1, 2, '/1/30/32/', 0, NOW(), NOW(), 0),
    (33, 1, 30,  '字节码执行', '#a78bfa', 2, 2, '/1/30/33/', 0, NOW(), NOW(), 0),
    (34, 1, 30,  '性能调优', '#a78bfa', 3, 2, '/1/30/34/', 0, NOW(), NOW(), 0),
    -- 框架生态
    (40, 1, 1,   '框架生态', '#f59e0b', 3, 1, '/1/40/',   0, NOW(), NOW(), 0),
    (41, 1, 40,  'Spring 家族', '#fbbf24', 0, 2, '/1/40/41/', 0, NOW(), NOW(), 0),
    (42, 1, 40,  'MyBatis 生态', '#fbbf24', 1, 2, '/1/40/42/', 0, NOW(), NOW(), 0),
    (43, 1, 40,  'ORM 框架', '#fbbf24', 2, 2, '/1/40/43/', 0, NOW(), NOW(), 0),
    -- 分布式
    (50, 1, 1,   '分布式系统', '#ef4444', 4, 1, '/1/50/',   0, NOW(), NOW(), 0),
    (51, 1, 50,  '微服务架构', '#f87171', 0, 2, '/1/50/51/', 0, NOW(), NOW(), 0),
    (52, 1, 50,  '消息队列', '#f87171', 1, 2, '/1/50/52/', 0, NOW(), NOW(), 0),
    (53, 1, 50,  '缓存系统', '#f87171', 2, 2, '/1/50/53/', 0, NOW(), NOW(), 0),
    (54, 1, 50,  '注册中心', '#f87171', 3, 2, '/1/50/54/', 0, NOW(), NOW(), 0),
    (55, 1, 50,  '分布式锁', '#f87171', 4, 2, '/1/50/55/', 0, NOW(), NOW(), 0),
    (56, 1, 50,  '分布式事务', '#f87171', 5, 2, '/1/50/56/', 0, NOW(), NOW(), 0),
    -- 数据库
    (60, 1, 1,   '数据库', '#06b6d4', 5, 1, '/1/60/',     0, NOW(), NOW(), 0),
    (61, 1, 60,  'MySQL', '#22d3ee', 0, 2, '/1/60/61/', 0, NOW(), NOW(), 0),
    (62, 1, 60,  'NoSQL', '#22d3ee', 1, 2, '/1/60/62/', 0, NOW(), NOW(), 0),
    -- DevOps
    (70, 1, 1,   'DevOps', '#84cc16', 6, 1, '/1/70/',    0, NOW(), NOW(), 0),
    (80, 1, 1,   '工具类', '#ec4899', 7, 1, '/1/80/',    0, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name), updated_at = NOW();

-- -------------------------------------------------------
-- 初始数据 - 小工具 (pb_widget / pb_ledger_dict)
-- -------------------------------------------------------
-- 6 个 widget (固定 ID 便于维护)
-- icon 存 Element Plus Icons 的图标名 (前端 constants/widgetIcons.ts 里有对应 key), 不用 emoji
INSERT INTO pb_widget (id, code, name, description, icon, icon_color, path, sort_order, status, click_count, created_at, updated_at, deleted)
VALUES
    (1, 'typing',   '打字练习', '英文打字练习, 虚拟键盘高亮', 'EditPen',    '#3b82f6', '/widget/typing',   0, 1, 0, NOW(), NOW(), 0),
    (2, 'random',   '随机数',   '指定范围生成随机整数',         'Operation',  '#10b981', '/widget/random',   1, 1, 0, NOW(), NOW(), 0),
    (4, 'ledger',   '记账',     '快速记录每笔收支',             'Wallet',     '#f59e0b', '/widget/ledger',   3, 1, 0, NOW(), NOW(), 0),
    (11, 'networth', '身家',    '按 活钱 / 定期 / 0 余额 三维度记录身家', 'CreditCard',  '#3b82f6', '/widget/networth', 4, 1, 0, NOW(), NOW(), 0),
    (12, 'recovery', '回本之路', '多仓位成本+市值, 追踪回本进度',         'TrendCharts', '#10b981', '/widget/recovery', 5, 1, 0, NOW(), NOW(), 0),
    (13, 'fund',     '投资表',  '基金/股票/ETF 成本+净值, 计算盈亏',    'DataLine',    '#ef4444', '/widget/fund',     6, 1, 0, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name), icon = VALUES(icon), icon_color = VALUES(icon_color), path = VALUES(path), sort_order = VALUES(sort_order), updated_at = NOW();

-- 5 个平台 (type=1) — icon 留空, 前端按 dictId 在 PLATFORM_ICONS 映射
INSERT INTO pb_ledger_dict (id, type, name, color, icon, sort_order, status, created_at, updated_at, deleted)
VALUES
    (101, 1, '抖音',   '#fe2c55', NULL, 0, 1, NOW(), NOW(), 0),
    (102, 1, '小红书', '#ff2442', NULL, 1, 1, NOW(), NOW(), 0),
    (103, 1, '淘宝',   '#ff6f00', NULL, 2, 1, NOW(), NOW(), 0),
    (104, 1, '微信',   '#07c160', NULL, 3, 1, NOW(), NOW(), 0),
    (105, 1, '支付宝', '#1677ff', NULL, 4, 1, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name), updated_at = NOW();

-- 8 个项目 (type=2) — icon 给默认 Element Plus Icons key, 用户新建项目时从前端图标网格选可覆盖
INSERT INTO pb_ledger_dict (id, type, name, color, icon, sort_order, status, created_at, updated_at, deleted)
VALUES
    (201, 2, '早饭', '#f97316', 'Dish',     0, 1, NOW(), NOW(), 0),
    (202, 2, '午饭', '#f59e0b', 'Bowl',     1, 1, NOW(), NOW(), 0),
    (203, 2, '晚饭', '#ef4444', 'Food',     2, 1, NOW(), NOW(), 0),
    (204, 2, '夜宵', '#8b5cf6', 'Moon',     3, 1, NOW(), NOW(), 0),
    (205, 2, '房租', '#6366f1', 'House',    4, 1, NOW(), NOW(), 0),
    (206, 2, '水电', '#0ea5e9', 'Lightning',5, 1, NOW(), NOW(), 0),
    (207, 2, '交通', '#10b981', 'Coin',     6, 1, NOW(), NOW(), 0),
    (208, 2, '其他', '#6b7280', 'Wallet',   7, 1, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name), updated_at = NOW();

-- -------------------------------------------------------
-- 初始数据 - 身家 (pb_account_category / pb_account)
-- -------------------------------------------------------
-- 3 个分类
INSERT INTO pb_account_category (id, name, sort_order, is_counted_in_total, status, created_at, updated_at, deleted)
VALUES
    (1, '活钱',   1, 1, 1, NOW(), NOW(), 0),
    (2, '定期',   2, 1, 1, NOW(), NOW(), 0),
    (3, '0 余额', 3, 0, 1, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name), updated_at = NOW();

-- 12 个账户
INSERT INTO pb_account (id, name, icon, color, sort_order, status, created_at, updated_at, deleted)
VALUES
    (1,  '微信零钱',         NULL, NULL, 0, 1, NOW(), NOW(), 0),
    (2,  '微信零钱通',       NULL, NULL, 1, 1, NOW(), NOW(), 0),
    (3,  '支付宝余额宝',     NULL, NULL, 2, 1, NOW(), NOW(), 0),
    (4,  '光大银行工资卡',   NULL, NULL, 3, 1, NOW(), NOW(), 0),
    (5,  '基金',             NULL, NULL, 4, 1, NOW(), NOW(), 0),
    (6,  '广发证券',         NULL, NULL, 5, 1, NOW(), NOW(), 0),
    (7,  '银河证券',         NULL, NULL, 6, 1, NOW(), NOW(), 0),
    (8,  '工商个人养老金账户', NULL, NULL, 7, 1, NOW(), NOW(), 0),
    (9,  '工商银行',         NULL, NULL, 8, 1, NOW(), NOW(), 0),
    (10, '农行银行',         NULL, NULL, 9, 1, NOW(), NOW(), 0),
    (11, '建设银行',         NULL, NULL, 10, 1, NOW(), NOW(), 0),
    (12, '山西联社',         NULL, NULL, 11, 1, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name), updated_at = NOW();
