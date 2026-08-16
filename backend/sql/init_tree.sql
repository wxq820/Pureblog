-- ================================================================
-- PureBlog Tree Catalog Module - Schema Init (MySQL 8.0+)
-- ================================================================
-- Apply manually: source this file in your `pureblog` database.
-- 项目无 Flyway / Liquibase,每次新增表都需要手动执行.

USE pureblog;

-- ------------------------------------------------------------
-- 1) 目录树主表 (pb_tree)
-- ------------------------------------------------------------
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

-- ------------------------------------------------------------
-- 2) 目录树节点表 (pb_tree_node)
-- ------------------------------------------------------------
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

-- ------------------------------------------------------------
-- 3) 文章表新增挂载字段 (pb_article)
-- ------------------------------------------------------------
-- 注意: 表已存在则只 ADD COLUMN;首次部署可直接复制整张表结构.
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pb_article' AND COLUMN_NAME = 'tree_node_id'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE pb_article ADD COLUMN tree_node_id BIGINT DEFAULT NULL COMMENT ''目录树叶子节点''',
    'SELECT ''tree_node_id column exists'' AS info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pb_article' AND COLUMN_NAME = 'tree_id'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE pb_article ADD COLUMN tree_id BIGINT DEFAULT NULL COMMENT ''目录树主表 ID''',
    'SELECT ''tree_id column exists'' AS info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pb_article' AND INDEX_NAME = 'idx_pb_article_tree_node'
);
SET @ddl := IF(@idx_exists = 0,
    'ALTER TABLE pb_article ADD INDEX idx_pb_article_tree_node (tree_node_id)',
    'SELECT ''idx_pb_article_tree_node exists'' AS info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pb_article' AND INDEX_NAME = 'idx_pb_article_tree'
);
SET @ddl := IF(@idx_exists = 0,
    'ALTER TABLE pb_article ADD INDEX idx_pb_article_tree (tree_id)',
    'SELECT ''idx_pb_article_tree exists'' AS info');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 4) 灌入一棵「Java 技术栈」初始数据 (从旧前端硬编码迁移)
-- ------------------------------------------------------------
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
    (50, 1, 1,   '分布式系统', '#ef4444', 4, 1, '/1/50/', 0, NOW(), NOW(), 0),
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

SELECT 'Tree module schema applied successfully' AS result;
