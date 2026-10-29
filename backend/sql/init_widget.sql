-- ================================================================
-- PureBlog Widget Module - Schema Init (MySQL 8.0+)
-- ================================================================
-- Apply manually: source this file in your `pureblog` database.
-- 项目无 Flyway / Liquibase,每次新增表都需要手动执行.

USE pureblog;

-- ------------------------------------------------------------
-- 1) 小工具主表 (pb_widget)
-- ------------------------------------------------------------
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

-- ------------------------------------------------------------
-- 2) 小工具点击流水 (pb_widget_click)
-- ------------------------------------------------------------
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

-- ------------------------------------------------------------
-- 3) 记账字典表 (pb_ledger_dict) - 平台 + 项目合一, type 区分
-- ------------------------------------------------------------
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

-- ------------------------------------------------------------
-- 4) 记账主表 (pb_ledger_record)
-- ------------------------------------------------------------
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

-- ------------------------------------------------------------
-- 0) v1 migration: 删除已废弃的 record_hour 列 + 清空旧 emoji 图标
-- ------------------------------------------------------------
-- 必须放在全部 CREATE TABLE 之后:
-- 全新库执行时表尚不存在, 提前跑会因 "Table doesn't exist" 整脚本失败.
-- MySQL 8.0 < 8.0.29 不支持 DROP COLUMN IF EXISTS, 用存储过程包一层
DROP PROCEDURE IF EXISTS migrate_drop_record_hour;
DELIMITER //
CREATE PROCEDURE migrate_drop_record_hour()
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'pb_ledger_record'
      AND column_name = 'record_hour'
  ) THEN
    ALTER TABLE pb_ledger_record DROP COLUMN record_hour;
  END IF;
END //
DELIMITER ;
CALL migrate_drop_record_hour();
DROP PROCEDURE migrate_drop_record_hour;
UPDATE pb_ledger_dict SET icon = NULL WHERE icon IS NOT NULL;

-- ================================================================
-- 灌入初始数据
-- ================================================================

-- 3 个 widget (固定 ID 便于维护)
-- icon 存 Element Plus Icons 的图标名 (前端 constants/widgetIcons.ts 里有对应 key), 不用 emoji
INSERT INTO pb_widget (id, code, name, description, icon, icon_color, path, sort_order, status, click_count, created_at, updated_at, deleted)
VALUES
    (1, 'typing',   '打字练习', '英文打字练习, 虚拟键盘高亮', 'EditPen',    '#3b82f6', '/widget/typing',   0, 1, 0, NOW(), NOW(), 0),
    (2, 'random',   '随机数',   '指定范围生成随机整数',         'Operation',  '#10b981', '/widget/random',   1, 1, 0, NOW(), NOW(), 0),
    (4, 'ledger',   '记账',     '快速记录每笔收支',             'Wallet',     '#f59e0b', '/widget/ledger',   3, 1, 0, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name), updated_at = NOW();

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

SELECT 'Widget module schema applied successfully' AS result;

-- ================================================================
-- v2 migration: pb_widget.icon 从 emoji 迁移为 Element Plus Icons 图标名
-- ------------------------------------------------------------
-- 幂等: 按 code 定位, 重复执行结果一致.
-- 图标名取自前端 frontend/pureblog-portal/src/constants/widgetIcons.ts.
UPDATE pb_widget SET icon = 'EditPen'   WHERE code = 'typing';
UPDATE pb_widget SET icon = 'Operation' WHERE code = 'random';
UPDATE pb_widget SET icon = 'Wallet'    WHERE code = 'ledger';
UPDATE pb_widget SET icon = 'CreditCard' WHERE code = 'networth';
UPDATE pb_widget SET icon = 'TrendCharts' WHERE code = 'recovery';
UPDATE pb_widget SET icon = 'DataLine'  WHERE code = 'fund';

-- 兜底: icon 为空, 或仍是非 ASCII 字符 (emoji 等) 的记录, 统一给默认图标.
-- Element Plus 图标名都是纯 ASCII 字母, 所以用 REGEXP '[^A-Za-z]' 判定.
UPDATE pb_widget SET icon = 'Grid'
WHERE icon IS NULL
   OR TRIM(icon) = ''
   OR icon REGEXP '[^A-Za-z]';
