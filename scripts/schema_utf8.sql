-- ============================================================
-- PureBlog Database Schema
-- MySQL 8.0
-- ============================================================

CREATE DATABASE IF NOT EXISTS pureblog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pureblog;

-- -------------------------------------------------------
-- 1. pb_user 鐢ㄦ埛琛?-- -------------------------------------------------------
CREATE TABLE pb_user (
    id              BIGINT UNSIGNED    NOT NULL AUTO_INCREMENT  COMMENT '鐢ㄦ埛ID',
    username        VARCHAR(50)       NOT NULL                COMMENT '鐢ㄦ埛鍚?,
    email           VARCHAR(255)      NOT NULL                COMMENT '閭',
    password_hash   VARCHAR(255)      NOT NULL                COMMENT '瀵嗙爜鍝堝笇',
    nickname        VARCHAR(100)      DEFAULT NULL            COMMENT '鏄电О',
    avatar_url      VARCHAR(500)      DEFAULT NULL            COMMENT '澶村儚URL',
    bio             VARCHAR(500)      DEFAULT NULL            COMMENT '涓汉绠€浠?,
    role            TINYINT UNSIGNED  NOT NULL DEFAULT 1       COMMENT '瑙掕壊: 1-鏅€氱敤鎴?2-浣滆€?3-绠＄悊鍛?4-瓒呯',
    status          TINYINT UNSIGNED  NOT NULL DEFAULT 1       COMMENT '鐘舵€? 1-姝ｅ父 2-绂佺敤',
    follower_count  INT UNSIGNED       NOT NULL DEFAULT 0      COMMENT '绮変笣鏁?,
    following_count INT UNSIGNED       NOT NULL DEFAULT 0      COMMENT '鍏虫敞鏁?,
    article_count   INT UNSIGNED       NOT NULL DEFAULT 0      COMMENT '鏂囩珷鏁?,
    last_login_at   DATETIME          DEFAULT NULL            COMMENT '鏈€鍚庣櫥褰曟椂闂?,
    created_at      DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    updated_at      DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    deleted         TINYINT UNSIGNED  NOT NULL DEFAULT 0      COMMENT '杞垹闄? 0-鍚?1-鏄?,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鐢ㄦ埛琛?;

-- -------------------------------------------------------
-- 2. pb_category 鍒嗙被琛?-- -------------------------------------------------------
CREATE TABLE pb_category (
    id          BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '鍒嗙被ID',
    name        VARCHAR(50)       NOT NULL                COMMENT '鍒嗙被鍚嶇О',
    slug        VARCHAR(50)       NOT NULL                COMMENT '鍒嗙被鍒悕',
    description VARCHAR(255)      DEFAULT NULL            COMMENT '鍒嗙被鎻忚堪',
    sort_order  INT UNSIGNED      NOT NULL DEFAULT 0      COMMENT '鎺掑簭',
    article_count INT UNSIGNED     NOT NULL DEFAULT 0      COMMENT '鏂囩珷鏁?,
    created_at  DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    updated_at  DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    deleted     TINYINT UNSIGNED   NOT NULL DEFAULT 0      COMMENT '杞垹闄?,
    PRIMARY KEY (id),
    UNIQUE KEY uk_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鍒嗙被琛?;

-- -------------------------------------------------------
-- 3. pb_tag 鏍囩琛?-- -------------------------------------------------------
CREATE TABLE pb_tag (
    id           BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '鏍囩ID',
    name         VARCHAR(50)      NOT NULL                 COMMENT '鏍囩鍚嶇О',
    slug         VARCHAR(50)      NOT NULL                 COMMENT '鏍囩鍒悕',
    article_count INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '鏂囩珷鏁?,
    created_at   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    updated_at   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    deleted      TINYINT UNSIGNED  NOT NULL DEFAULT 0      COMMENT '杞垹闄?,
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name),
    UNIQUE KEY uk_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鏍囩琛?;

-- -------------------------------------------------------
-- 4. pb_article 鏂囩珷鍏冩暟鎹〃
-- -------------------------------------------------------
CREATE TABLE pb_article (
    id              BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT  COMMENT '鏂囩珷ID',
    author_id       BIGINT UNSIGNED   NOT NULL                 COMMENT '浣滆€匢D',
    category_id     BIGINT UNSIGNED   DEFAULT NULL             COMMENT '鍒嗙被ID',
    title           VARCHAR(255)      NOT NULL                 COMMENT '鏂囩珷鏍囬',
    summary         VARCHAR(500)      DEFAULT NULL             COMMENT '鏂囩珷鎽樿',
    cover_url       VARCHAR(500)      DEFAULT NULL             COMMENT '灏侀潰鍥綰RL',
    status          TINYINT UNSIGNED  NOT NULL DEFAULT 0       COMMENT '鐘舵€? 0-鑽夌 1-宸插彂甯?2-宸蹭笅鏋?,
    view_count      INT UNSIGNED      NOT NULL DEFAULT 0       COMMENT '娴忚閲?,
    like_count      INT UNSIGNED      NOT NULL DEFAULT 0       COMMENT '鐐硅禐鏁?,
    comment_count   INT UNSIGNED      NOT NULL DEFAULT 0       COMMENT '璇勮鏁?,
    collect_count   INT UNSIGNED      NOT NULL DEFAULT 0       COMMENT '鏀惰棌鏁?,
    is_featured     TINYINT UNSIGNED  NOT NULL DEFAULT 0       COMMENT '鏄惁绮鹃€?,
    is_top          TINYINT UNSIGNED  NOT NULL DEFAULT 0       COMMENT '鏄惁缃《',
    published_at    DATETIME          DEFAULT NULL             COMMENT '鍙戝竷鏃堕棿',
    created_at      DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    updated_at      DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    deleted         TINYINT UNSIGNED   NOT NULL DEFAULT 0       COMMENT '杞垹闄?,
    PRIMARY KEY (id),
    KEY idx_author_id (author_id),
    KEY idx_category_id (category_id),
    KEY idx_status (status),
    KEY idx_published_at (published_at),
    KEY idx_is_featured (is_featured),
    KEY idx_is_top (is_top)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鏂囩珷鍏冩暟鎹〃';

-- -------------------------------------------------------
-- 5. pb_article_content 鏂囩珷姝ｆ枃琛?-- -------------------------------------------------------
CREATE TABLE pb_article_content (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '鍐呭ID',
    article_id  BIGINT UNSIGNED  NOT NULL                COMMENT '鏂囩珷ID',
    content     LONGTEXT         NOT NULL                COMMENT 'Markdown姝ｆ枃',
    html_content LONGTEXT        DEFAULT NULL             COMMENT '娓叉煋鍚嶩TML',
    word_count  INT UNSIGNED     NOT NULL DEFAULT 0      COMMENT '瀛楁暟',
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    updated_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    PRIMARY KEY (id),
    UNIQUE KEY uk_article_id (article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鏂囩珷姝ｆ枃琛?;

-- -------------------------------------------------------
-- 6. pb_article_tag 鏂囩珷鏍囩鍏宠仈琛?-- -------------------------------------------------------
CREATE TABLE pb_article_tag (
    id         BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    article_id BIGINT UNSIGNED  NOT NULL                 COMMENT '鏂囩珷ID',
    tag_id     BIGINT UNSIGNED  NOT NULL                 COMMENT '鏍囩ID',
    created_at DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    PRIMARY KEY (id),
    UNIQUE KEY uk_article_tag (article_id, tag_id),
    KEY idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鏂囩珷鏍囩鍏宠仈琛?;

-- -------------------------------------------------------
-- 7. pb_comment 璇勮琛?-- -------------------------------------------------------
CREATE TABLE pb_comment (
    id           BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '璇勮ID',
    article_id   BIGINT UNSIGNED  NOT NULL                 COMMENT '鏂囩珷ID',
    user_id      BIGINT UNSIGNED  NOT NULL                 COMMENT '璇勮鐢ㄦ埛ID',
    parent_id    BIGINT UNSIGNED  DEFAULT NULL             COMMENT '鐖惰瘎璁篒D(0琛ㄧず椤剁骇)',
    reply_to_id  BIGINT UNSIGNED  DEFAULT NULL             COMMENT '鍥炲鐩爣璇勮ID',
    reply_to_uid BIGINT UNSIGNED  DEFAULT NULL             COMMENT '鍥炲鐩爣鐢ㄦ埛ID',
    content      VARCHAR(1000)    NOT NULL                 COMMENT '璇勮鍐呭',
    like_count   INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '鐐硅禐鏁?,
    status       TINYINT UNSIGNED  NOT NULL DEFAULT 0       COMMENT '鐘舵€? 0-寰呭鏍?1-閫氳繃 2-鎷掔粷',
    created_at   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    updated_at   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    deleted      TINYINT UNSIGNED  NOT NULL DEFAULT 0       COMMENT '杞垹闄?,
    PRIMARY KEY (id),
    KEY idx_article_id (article_id),
    KEY idx_user_id (user_id),
    KEY idx_parent_id (parent_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='璇勮琛?;

-- -------------------------------------------------------
-- 8. pb_follow 鍏虫敞鍏崇郴琛?-- -------------------------------------------------------
CREATE TABLE pb_follow (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    follower_id BIGINT UNSIGNED  NOT NULL                 COMMENT '绮変笣ID',
    following_id BIGINT UNSIGNED NOT NULL                 COMMENT '鍏虫敞鐢ㄦ埛ID',
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    PRIMARY KEY (id),
    UNIQUE KEY uk_follow (follower_id, following_id),
    KEY idx_following_id (following_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鍏虫敞鍏崇郴琛?;

-- -------------------------------------------------------
-- 9. pb_collect 鏀惰棌琛?-- -------------------------------------------------------
CREATE TABLE pb_collect (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    user_id     BIGINT UNSIGNED  NOT NULL                 COMMENT '鐢ㄦ埛ID',
    article_id  BIGINT UNSIGNED  NOT NULL                 COMMENT '鏂囩珷ID',
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鏀惰棌鏃堕棿',
    PRIMARY KEY (id),
    UNIQUE KEY uk_collect (user_id, article_id),
    KEY idx_article_id (article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鏀惰棌琛?;

-- -------------------------------------------------------
-- 10. pb_like 鐐硅禐琛?鏂囩珷/璇勮鐐硅禐鍏辩敤)
-- -------------------------------------------------------
CREATE TABLE pb_like (
    id         BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    user_id    BIGINT UNSIGNED  NOT NULL                 COMMENT '鐢ㄦ埛ID',
    target_id  BIGINT UNSIGNED  NOT NULL                 COMMENT '鐩爣ID',
    target_type TINYINT UNSIGNED NOT NULL                COMMENT '绫诲瀷: 1-鏂囩珷 2-璇勮',
    created_at DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鐐硅禐鏃堕棿',
    PRIMARY KEY (id),
    UNIQUE KEY uk_like (user_id, target_id, target_type),
    KEY idx_target (target_id, target_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鐐硅禐琛?;

-- -------------------------------------------------------
-- 11. pb_notification 閫氱煡琛?-- -------------------------------------------------------
CREATE TABLE pb_notification (
    id           BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '閫氱煡ID',
    user_id      BIGINT UNSIGNED  NOT NULL                 COMMENT '鎺ユ敹鐢ㄦ埛ID',
    type         TINYINT UNSIGNED NOT NULL                 COMMENT '绫诲瀷: 1-璇勮 2-鍥炲 3-鍏虫敞 4-鐐硅禐 5-鏀惰棌 6-绯荤粺閫氱煡',
    title        VARCHAR(255)     DEFAULT NULL             COMMENT '閫氱煡鏍囬',
    content      VARCHAR(1000)   DEFAULT NULL             COMMENT '閫氱煡鍐呭',
    related_id   BIGINT UNSIGNED  DEFAULT NULL             COMMENT '鍏宠仈ID(鏂囩珷ID/璇勮ID绛?',
    related_type TINYINT UNSIGNED DEFAULT NULL             COMMENT '鍏宠仈绫诲瀷',
    is_read      TINYINT UNSIGNED NOT NULL DEFAULT 0       COMMENT '鏄惁宸茶: 0-鍚?1-鏄?,
    created_at   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_is_read (is_read),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='閫氱煡琛?;

-- -------------------------------------------------------
-- 12. pb_login_log 鐧诲綍鏃ュ織琛?-- -------------------------------------------------------
CREATE TABLE pb_login_log (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    user_id     BIGINT UNSIGNED  DEFAULT NULL             COMMENT '鐢ㄦ埛ID',
    username    VARCHAR(50)      DEFAULT NULL             COMMENT '鐢ㄦ埛鍚?,
    ip          VARCHAR(50)      DEFAULT NULL             COMMENT '鐧诲綍IP',
    user_agent  VARCHAR(500)     DEFAULT NULL             COMMENT 'User-Agent',
    status      TINYINT UNSIGNED NOT NULL DEFAULT 1       COMMENT '鐘舵€? 1-鎴愬姛 2-澶辫触',
    msg         VARCHAR(255)     DEFAULT NULL             COMMENT '澶囨敞淇℃伅',
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鐧诲綍鏃堕棿',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鐧诲綍鏃ュ織琛?;

-- -------------------------------------------------------
-- 13. pb_kafka_outbox Kafka Outbox琛?淇濊瘉娑堟伅鍙潬鎶曢€?
-- -------------------------------------------------------
CREATE TABLE pb_kafka_outbox (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    topic       VARCHAR(100)     NOT NULL                 COMMENT 'Kafka Topic',
    key         VARCHAR(255)     DEFAULT NULL             COMMENT '娑堟伅Key',
    payload     TEXT             NOT NULL                 COMMENT '娑堟伅鍐呭(JSON)',
    status      TINYINT UNSIGNED NOT NULL DEFAULT 0       COMMENT '鐘舵€? 0-寰呭彂閫?1-宸插彂閫?2-澶辫触',
    retry_count INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '閲嶈瘯娆℃暟',
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    sent_at     DATETIME         DEFAULT NULL             COMMENT '鍙戦€佹椂闂?,
    PRIMARY KEY (id),
    KEY idx_status (status),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Kafka Outbox琛?;

-- -------------------------------------------------------
-- 14. pb_article_stats 鏂囩珷缁熻琛?瀹氭椂鑱氬悎)
-- -------------------------------------------------------
CREATE TABLE pb_article_stats (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    article_id  BIGINT UNSIGNED  NOT NULL                 COMMENT '鏂囩珷ID',
    pv          INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '椤甸潰娴忚閲?,
    uv          INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '鐙珛璁垮鏁?,
    date        DATE             NOT NULL                 COMMENT '缁熻鏃ユ湡',
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    updated_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    PRIMARY KEY (id),
    UNIQUE KEY uk_article_date (article_id, date),
    KEY idx_date (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鏂囩珷缁熻琛?;

-- -------------------------------------------------------
-- 15. pb_file 鏂囦欢琛?-- -------------------------------------------------------
CREATE TABLE pb_file (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    uploader_id BIGINT UNSIGNED  NOT NULL                 COMMENT '涓婁紶鑰匢D',
    file_name   VARCHAR(255)     NOT NULL                 COMMENT '鏂囦欢鍚?,
    file_key    VARCHAR(255)     NOT NULL                 COMMENT 'MinIO瀛樺偍Key',
    file_url    VARCHAR(500)     NOT NULL                 COMMENT '璁块棶URL',
    file_size   BIGINT UNSIGNED  NOT NULL                 COMMENT '鏂囦欢澶у皬(瀛楄妭)',
    mime_type   VARCHAR(100)     DEFAULT NULL             COMMENT 'MIME绫诲瀷',
    file_type   TINYINT UNSIGNED NOT NULL                 COMMENT '鏂囦欢绫诲瀷: 1-鍥剧墖 2-瑙嗛 3-鏂囨。 4-鍏朵粬',
    bucket      VARCHAR(50)      DEFAULT NULL             COMMENT '瀛樺偍妗?,
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '涓婁紶鏃堕棿',
    PRIMARY KEY (id),
    KEY idx_uploader_id (uploader_id),
    KEY idx_file_type (file_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鏂囦欢琛?;

-- -------------------------------------------------------
-- 16. pb_system_config 绯荤粺閰嶇疆琛?-- -------------------------------------------------------
CREATE TABLE pb_system_config (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    config_key  VARCHAR(100)     NOT NULL                 COMMENT '閰嶇疆閿?,
    config_value TEXT            DEFAULT NULL             COMMENT '閰嶇疆鍊?,
    description VARCHAR(255)     DEFAULT NULL             COMMENT '鎻忚堪',
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    updated_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='绯荤粺閰嶇疆琛?;

-- -------------------------------------------------------
-- 鍒濆鏁版嵁
-- -------------------------------------------------------

-- 鎻掑叆瓒呯璐︽埛 (瀵嗙爜: admin123, BCrypt鍔犲瘑)
INSERT INTO pb_user (username, email, password_hash, nickname, role, status) VALUES
('admin', 'admin@pureblog.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '绠＄悊鍛?, 4, 1);

-- 鎻掑叆榛樿鍒嗙被
INSERT INTO pb_category (name, slug, description, sort_order) VALUES
('鎶€鏈?, 'tech', '鎶€鏈浉鍏虫枃绔?, 1),
('鐢熸椿', 'life', '鐢熸椿闅忕瑪', 2),
('璇讳功', 'reading', '璇讳功绗旇', 3),
('闅忔兂', 'thoughts', '闅忔兂鏉傝皥', 4);

-- 鎻掑叆榛樿鏍囩
INSERT INTO pb_tag (name, slug) VALUES
('Java', 'java'),
('Spring Boot', 'spring-boot'),
('Redis', 'redis'),
('MySQL', 'mysql'),
('Elasticsearch', 'elasticsearch'),
('Kafka', 'kafka'),
('Docker', 'docker'),
('Vue', 'vue'),
('鏋舵瀯璁捐', 'architecture'),
('鎬ц兘浼樺寲', 'performance');

-- 鎻掑叆榛樿绯荤粺閰嶇疆
INSERT INTO pb_system_config (config_key, config_value, description) VALUES
('site_name', 'PureBlog', '缃戠珯鍚嶇О'),
('site_description', '绾补鐨勬妧鏈崥瀹㈠钩鍙?, '缃戠珯鎻忚堪'),
('article_per_page', '10', '姣忛〉鏂囩珷鏁?),
('hot_article_days', '7', '鐑缁熻澶╂暟'),
('comment_need_audit', 'true', '璇勮鏄惁闇€瑕佸鏍?),
('upload_max_size', '10485760', '鏂囦欢涓婁紶澶у皬闄愬埗(瀛楄妭)');
