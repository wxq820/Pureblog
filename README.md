# PureBlog

一个面向内容平台场景的 Spring Boot 3 多模块博客系统。围绕**文章发布链路、全文搜索链路、热点读取链路**展示缓存、异步事件、全文检索、最终一致性与可观测能力。

## 1. 项目定位

PureBlog 不是"写文章的网站"，而是一个具备工程表达力的内容平台：

- 对外：文章展示、全文搜索、评论互动、点赞收藏、关注通知
- 对内：文章管理、分类标签管理、评论审核、用户与角色管理、运营统计
- 技术上：Spring Boot 3 + MySQL + Redis + Elasticsearch + Spring 事件驱动
- 工程上：缓存一致性、事件驱动、最终一致性、可观测、容器化部署

## 2. 技术栈一览

| 类别 | 选型 |
| --- | --- |
| 语言 | Java 17 |
| 框架 | Spring Boot 3.2.5 |
| 持久化 | MySQL 8.0 + MyBatis-Plus 3.5.11 |
| 缓存 | Redis 7 + 自定义 `RedisTemplate` + Lettuce 连接池 |
| 搜索 | Elasticsearch 8 + `co.elastic.clients` Java Client |
| 异步 | Spring `ApplicationEventPublisher` + `@EventListener` + `@EnableAsync` |
| 鉴权 | JWT (jjwt 0.12.6) + 自定义 `AuthInterceptor` |
| 安全 | Spring Security 3（仅做 CSRF/CORS 关闭，鉴权走自定义拦截器） |
| 密码 | Spring Security `DelegatingPasswordEncoder` (BCrypt) |
| 前端 | Vue 3 + TypeScript + Vite + Pinia + Vue Router + Element Plus |
| 工具 | Hutool、Lombok、Day.js |

### 关于异步消息

代码中**没有使用 Kafka**。所有"发布后异步处理"通过 Spring `ApplicationEventPublisher` 在同一 JVM 进程内分发，由 `@EventListener` 监听。事件定义在 `pureblog-common` 的 `event/` 包下：

- `ArticleEvent` —— 文章发布/更新/下线/删除
- `CommentCreatedEvent` —— 评论创建
- `FollowCreatedEvent` —— 关注关系
- `StatsLikeArticleEvent` —— 文章点赞
- `StatsPvEvent` —— 文章浏览

监听器在 `pureblog-search` 与 `pureblog-notification` 子模块内的 `listener/` 包下。

## 3. 仓库结构

```text
Pureblog/
├── backend/                          # Spring Boot 后端
│   ├── pom.xml                       # 父 POM（聚合所有模块）
│   ├── pureblog-app/                 # 主启动模块
│   ├── pureblog-common/              # 公共：响应、异常、事件、拦截器、枚举、工具
│   ├── pureblog-auth/                # 登录、注册、JWT、登录日志
│   ├── pureblog-user/                # 用户资料、关注关系
│   ├── pureblog-article/             # 文章、分类、标签、文章缓存
│   ├── pureblog-comment/             # 评论、点赞、收藏
│   ├── pureblog-search/              # Elasticsearch 索引同步与查询
│   ├── pureblog-notification/        # 站内通知
│   ├── pureblog-stats/               # PV/UV、热榜、仪表盘
│   └── pureblog-admin/               # 管理后台（用户/文章/评论/分类/标签）
├── frontend/
│   ├── pureblog-portal/              # 门户站点（Vue 3 + TS）
│   └── pureblog-admin-web/           # 管理后台（Vue 3 + TS + Element Plus）
├── docs/                             # 设计文档（与代码同步）
├── deploy/                           # Docker Compose 部署文件
└── scripts/                          # 启动/停止/初始化脚本
```

## 4. 快速开始

### 4.1 启动基础设施

```bash
cd deploy
docker compose -f docker-compose.dev.yml up -d
```

只启动 MySQL 和 Redis 即可。Elasticsearch 启动需要单独配置：

```bash
docker run -d --name pureblog-es \
  -p 9200:9200 -e "discovery.type=single-node" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  docker.elastic.co/elasticsearch/elasticsearch:8.13.0
```

### 4.2 初始化数据库

```bash
mysql -uroot -p123456 < scripts/schema.sql
```

### 4.3 启动后端

```bash
cd backend
mvn -pl pureblog-app -am spring-boot:run
```

默认管理员：`admin / admin123`（脚本 seed 数据）。

### 4.4 启动前端

```bash
cd frontend/pureblog-portal
npm install && npm run dev    # 门户 http://localhost:5173

cd ../pureblog-admin-web
npm install && npm run dev    # 管理后台 http://localhost:5174
```

### 4.2.1 新增「目录树」表 (一次性,Tree 模块启用前必做)

`pureblog` 库需要手动执行 `backend/sql/init_tree.sql`,新增 `pb_tree`、`pb_tree_node`,并给 `pb_article` 加 `tree_node_id`/`tree_id` 字段,同时灌一棵「Java 技术栈」初始树:

```bash
mysql -uroot -p123456 pureblog < backend/sql/init_tree.sql
```

## 5. 文档导览

| 文档 | 内容 |
| --- | --- |
| [docs/00_项目总览.md](docs/00_项目总览.md) | 项目定位、目标、模块、技术表达 |
| [docs/01_技术栈与技术背景.md](docs/01_技术栈与技术背景.md) | 完整选型与理由 |
| [docs/02_系统架构设计.md](docs/02_系统架构设计.md) | 当前架构与三条核心链路 |
| [docs/03_项目目录建议.md](docs/03_项目目录建议.md) | 实际目录结构与命名规范 |
| [docs/04_数据库设计.sql](docs/04_数据库设计.sql) | 完整建表脚本 |
| [docs/05_核心业务流程.md](docs/05_核心业务流程.md) | 发布/读取/搜索/评论/统计流程 |
| [docs/06_API接口清单.md](docs/06_API接口清单.md) | 当前所有 HTTP 接口 |
| [docs/07_开发任务拆解.md](docs/07_开发任务拆解.md) | 阶段、任务、优先级 |
| [docs/08_非功能需求与技术亮点.md](docs/08_非功能需求与技术亮点.md) | 性能、可用性、可观测性 |
| [docs/09_Cursor协作规则.md](docs/09_Cursor协作规则.md) | 给 Cursor 的硬性约束 |
| [docs/10_Cursor开发Prompt模板.md](docs/10_Cursor开发Prompt模板.md) | 可直接复制给 Cursor 的 Prompt |
| [docs/11_阶段性交付与验收清单.md](docs/11_阶段性交付与验收清单.md) | 验收清单 |
| [docs/12_建议的简历表达.md](docs/12_建议的简历表达.md) | 简历要点与面试表达 |

## 6. 核心链路速览

1. **发布链路**：作者发布 → 写 MySQL → 失效详情缓存 → 发布 `ArticleEvent` → 搜索模块同步索引 + 通知模块给关注者发通知
2. **读取链路**：访问详情 → 查 Redis → 未命中回源 MySQL → 回填 Redis → 自增 PV（Redis 计数器）
3. **搜索链路**：用户搜索 → `SearchService` 直接查 ES → 高亮返回 → 点击走详情缓存链路
4. **热点链路**：浏览 + 点赞事件实时累加 Redis ZSet 中的热度分 → 定时从 MySQL 全量刷新兜底

## 7. 测试账号

- 超管：`admin / admin123`
- 普通用户：注册即可
