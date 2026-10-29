# PureBlog

一个以**个人技术博客为主**的 Spring Boot 3 多模块博客系统。围绕**文章发布链路、全文搜索链路、热点读取链路、目录树导航链路**展示缓存、异步事件、全文检索、最终一致性与可观测能力。代码层面保留多人协作能力（登录/注册/关注/通知/管理后台接口齐全），部署时可按需启用。

## 1. 项目定位

PureBlog 不是「写文章的网站」，而是一个有工程表达力的博客平台：

- 对外：文章展示、全文搜索、评论互动、点赞收藏、关注通知、目录树导航
- 对内（按需）：文章管理、分类标签管理、目录树管理、评论审核、用户与角色管理、运营统计
- 技术上：Spring Boot 3 + MySQL + Redis + Spring 进程内事件
- 工程上：缓存一致性、事件驱动、最终一致性、可观测、容器化部署

> **默认定位**：个人博客 —— 单账号、新账号直接写库、不开放前端注册入口。多人协作能力代码完整保留。

## 2. 技术栈一览

| 类别 | 选型 |
| --- | --- |
| 语言 | Java 17 |
| 框架 | Spring Boot 3.2.5 |
| 持久化 | MySQL 8.0 + MyBatis-Plus 3.5.11 |
| 缓存 | Redis 7 + 自定义 `RedisTemplate` + Lettuce 连接池 |
| 搜索 | MySQL `LIKE` 多表 JOIN（标题/摘要/正文/标签）+ 服务端转义高亮 |
| 异步 | Spring `ApplicationEventPublisher` + `@EventListener` + `@EnableAsync`（**不引入 Kafka**） |
| 鉴权 | JWT (jjwt 0.12.6) + 自定义 `JwtAuthenticationFilter`（注册在 Spring Security 链中）+ `LoginUserHolder` ThreadLocal |
| 安全 | Spring Security 3（仅做 CSRF/CORS 关闭、过滤器链挂载，业务鉴权在自定义过滤器） |
| 密码 | Spring Security `DelegatingPasswordEncoder` (BCrypt) |
| 前端 | Vue 3 + TypeScript + Vite + Pinia + Vue Router + Element Plus |
| 工具 | Hutool、Lombok、Day.js |

### 关于异步消息

代码中**没有使用 Kafka**。所有「发布后异步处理」通过 Spring `ApplicationEventPublisher` 在同一 JVM 进程内分发，由 `@EventListener` 监听。事件定义在 `pureblog-common` 的 `event/` 包下：

- `CommentCreatedEvent` —— 评论创建
- `FollowCreatedEvent` —— 关注关系
- `StatsLikeArticleEvent` —— 文章点赞
- `StatsPvEvent` —— 文章浏览

监听器在 `pureblog-notification` 与 `pureblog-stats` 子模块内的 `listener/` 包下。

## 3. 仓库结构

```text
Pureblog/
├── backend/                          # Spring Boot 后端
│   ├── pom.xml                       # 父 POM（聚合所有模块）
│   ├── pureblog-app/                 # 主启动模块
│   ├── pureblog-common/              # 公共：响应、异常、事件、上下文、枚举、工具
│   ├── pureblog-auth/                # 登录、JWT 过滤器、登录日志
│   ├── pureblog-user/                # 用户资料、关注关系
│   ├── pureblog-article/             # 文章、分类、标签、文章缓存、全文检索（MySQL LIKE）
│   ├── pureblog-comment/             # 评论、点赞、收藏
│   ├── pureblog-notification/        # 站内通知
│   ├── pureblog-stats/               # PV/UV、热榜、仪表盘
│   ├── pureblog-tree/                # 目录树（pb_tree / pb_tree_node）+ 叶子校验
│   ├── pureblog-upload/              # 本地图片上传（UploadController + WebConfig 静态映射）
│   └── pureblog-admin/               # 管理后台接口（用户/文章/评论/分类/标签/树）
├── frontend/
│   └── pureblog-portal/              # 门户站点（Vue 3 + TS + Element Plus）
│   └── pureblog-admin-web/           # 管理后台 UI（按需启用）
├── docs/                             # 设计文档（与代码同步）
├── deploy/                           # Docker Compose 部署文件
└── scripts/                          # 启动/停止/初始化脚本
```

## 4. 快速开始

### 4.1 启动基础设施

```bash
# 项目根目录执行（唯一 compose 入口）
docker compose up -d
```

三容器：MySQL + Redis + JDK17/Maven。数据持久化在 Docker 命名卷 `pureblog-temp_mysql_data` / `pureblog-temp_redis_data`；
备份 / 恢复用 `scripts/backup-db.bat`（自动保留最近 14 天备份，见该脚本头部说明）。

### 4.2 初始化数据库

`scripts/schema.sql` 是 31 张表的全量建库入口（含目录树 2 张、小工具 4 张、财务 9 张）：

```bash
docker exec -i pureblog-mysql mysql -uroot -p123456 pureblog < scripts/schema.sql
```

### 4.3 初始化目录树 / 小工具 / 财务模块（增量补齐）

首次 `docker compose up -d` 时，`initdb.d` 会自动按 `01_` / `02_` / `03_` 顺序导入三个脚本。
若库已存在需要手动补齐（幂等 `INSERT ... ON DUPLICATE KEY UPDATE`）：

```bash
docker exec -i pureblog-mysql mysql -uroot -p123456 pureblog < backend/sql/init_tree.sql
docker exec -i pureblog-mysql mysql -uroot -p123456 pureblog < backend/sql/init_widget.sql
docker exec -i pureblog-mysql mysql -uroot -p123456 pureblog < backend/sql/init_finance.sql
```

> `scripts\start-all.bat` 会自动完成第 1~4 步：一键启动基础设施 → 导入 SQL → 构建后端 → 启动后端 → 启动前端 → 轮询健康检查。

### 4.4 启动后端

```bash
cd backend
mvn -pl pureblog-app -am spring-boot:run
```

默认管理员：`admin / 123456`（脚本 seed 数据）。

> 新增用户直接在 `pb_user` 表 INSERT，密码用 BCrypt 哈希（前端**默认不暴露**注册入口）。

### 4.5 启动前端

```bash
cd frontend/pureblog-portal
npm install && npm run dev    # 门户 http://localhost:3000
```

## 5. 文档导览

| 文档 | 内容 |
| --- | --- |
| [docs/00_项目总览.md](docs/00_项目总览.md) | 项目定位、目标、模块、技术表达 |
| [docs/01_技术栈与技术背景.md](docs/01_技术栈与技术背景.md) | 完整选型与理由 |
| [docs/02_系统架构设计.md](docs/02_系统架构设计.md) | 当前架构与四条核心链路 |
| [docs/03_项目目录建议.md](docs/03_项目目录建议.md) | 实际目录结构与命名规范 |
| [docs/04_数据库设计.sql](docs/04_数据库设计.sql) | 31 张表设计说明（字段 / 用途 / 关系）；建库入口为 `scripts/schema.sql` |
| [docs/05_核心业务流程.md](docs/05_核心业务流程.md) | 发布/读取/搜索/评论/统计/目录树流程 |
| [docs/06_API接口清单.md](docs/06_API接口清单.md) | 当前所有 HTTP 接口（含 Tree 模块） |
| [docs/07_开发任务拆解.md](docs/07_开发任务拆解.md) | 阶段、任务、优先级 |
| [docs/08_非功能需求与技术亮点.md](docs/08_非功能需求与技术亮点.md) | 性能、可用性、可观测性 |
| [docs/09_Cursor协作规则.md](docs/09_Cursor协作规则.md) | 给 Cursor 的硬性约束 |
| [docs/10_Cursor开发Prompt模板.md](docs/10_Cursor开发Prompt模板.md) | 可直接复制给 Cursor 的 Prompt |
| [docs/11_阶段性交付与验收清单.md](docs/11_阶段性交付与验收清单.md) | 验收清单 |
| [docs/12_建议的简历表达.md](docs/12_建议的简历表达.md) | 简历要点与面试表达 |

> 文档维护规则：见 [.cursor/rules/docs-sync.mdc](.cursor/rules/docs-sync.mdc)。任何代码/SQL/接口/中间件变更必须同步 `docs/`。

## 6. 核心链路速览

1. **发布链路**：作者发布 → 校验 `treeService.assertIsLeaf(treeNodeId)` → 写 MySQL → 失效详情缓存 → 计数更新
2. **读取链路**：访问详情 → 查 Redis → 未命中回源 MySQL → 回填 Redis → 自增 PV（Redis 计数器）
3. **搜索链路**：用户搜索 → `SearchService` 走 MySQL 多表 JOIN + LIKE → 服务端转义高亮 → 点击走详情缓存链路
4. **热点链路**：浏览 + 点赞事件实时累加 Redis ZSet 中的热度分 → 定时从 MySQL 全量刷新兜底
5. **目录树导航链路**：SkillTree 选中节点 → URL 带 `?nodeId=X&treeId=Y` → HomePage watch → 文章列表按 `treeNodeId` 过滤

## 7. 测试账号

- 超管：`admin / 123456`（脚本 seed）
- 普通用户：直接在 `pb_user` 表 INSERT（前端默认不引导注册）
