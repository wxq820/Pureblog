-- ================================================================
-- PureBlog Finance Widgets - Schema Init (MySQL 8.0+)
-- ================================================================
-- 三个独立个人财务工具: 身家 / 回本之路 / 投资表
-- Apply: source this file in your `pureblog` database.
-- 项目无 Flyway / Liquibase, 每次新增表都需要手动执行.

USE pureblog;

-- ================================================================
-- #1 身家 (Net Worth) - 4 表 (per-snapshot category)
-- ================================================================

-- 1.1 分类/维度表
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

-- 1.2 具体账户 (无 category 字段, 分类跟随 entry)
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

-- 1.3 快照头
CREATE TABLE IF NOT EXISTS pb_balance_snapshot (
    id           BIGINT       NOT NULL PRIMARY KEY,
    snapshot_at  DATETIME     NOT NULL COMMENT '快照日期 (按时间排序+算 delta)',
    event_tag    VARCHAR(32)  DEFAULT NULL COMMENT '事件标签:发薪/20号/月底...',
    remark       VARCHAR(255) DEFAULT NULL,
    created_at   DATETIME     DEFAULT NULL,
    deleted      TINYINT      NOT NULL DEFAULT 0,
    KEY idx_pb_balance_snapshot_at (snapshot_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='身家:快照头';

-- 1.4 快照明细 (每行 = 一个账户在某个快照下的余额+当时的分类)
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

-- ================================================================
-- #2 回本之路 (Recovery) - 2 表
-- ================================================================

-- 2.1 仓位主表
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

-- 2.2 仓位市值流水
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

-- ================================================================
-- #3 投资表 (Fund) - 3 表
-- ================================================================

-- 3.1 基金/股票主表
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

-- 3.2 买入记录
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

-- 3.3 净值/价格流水
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

-- ================================================================
-- 灌入初始数据
-- ================================================================

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

-- 3 个 widget 入口 (固定 ID 11/12/13, 避开已有 1/2/4)
-- icon 存 Element Plus Icons 图标名 (前端 constants/widgetIcons.ts 里有对应 key)
INSERT INTO pb_widget (id, code, name, description, icon, icon_color, path, sort_order, status, click_count, created_at, updated_at, deleted)
VALUES
    (11, 'networth', '身家',     '按 活钱 / 定期 / 0 余额 三维度记录身家', 'CreditCard',  '#3b82f6', '/widget/networth', 4, 1, 0, NOW(), NOW(), 0),
    (12, 'recovery', '回本之路', '多仓位成本+市值, 追踪回本进度',          'TrendCharts', '#10b981', '/widget/recovery', 5, 1, 0, NOW(), NOW(), 0),
    (13, 'fund',     '投资表',   '基金/股票/ETF 成本+净值, 计算盈亏',     'DataLine',    '#ef4444', '/widget/fund',     6, 1, 0, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE name = VALUES(name), icon = VALUES(icon), icon_color = VALUES(icon_color), path = VALUES(path), sort_order = VALUES(sort_order), updated_at = NOW();

SELECT 'Finance widget module schema applied successfully' AS result;
