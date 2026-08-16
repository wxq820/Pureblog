# 09 Cursor 协作规则

> 这是给 Cursor 的硬性约束。每次修改前都要核对本文。

## 1. 总原则

- 严格以本仓库实际代码为准修改，**不要参考老的、未实现的接口或表结构**
- 鉴权走自定义 `AuthInterceptor`（不要加 Spring Security 过滤器链）
- 异步事件用 `ApplicationEventPublisher`（不要引入 Kafka）
- 一个任务只做一件事，不要"顺手"改其他模块
- 改动前先说明影响范围

---

## 2. 代码生成规则

1. 优先输出**完整文件**
2. 不要只给代码片段
3. 每次只实现一个明确模块或闭环
4. 不要同时改太多模块
5. 所有新增文件都要给路径
6. 改动接口或表结构时必须同步更新 `docs/06_API接口清单.md` / `docs/04_数据库设计.sql`

---

## 3. 模块边界

| 子模块 | 允许依赖 |
| --- | --- |
| `pureblog-common` | 不依赖其他子模块 |
| `pureblog-auth` | `pureblog-common` |
| `pureblog-user` | `pureblog-common`、`pureblog-auth`（共享 UserDO） |
| `pureblog-article` | `pureblog-common`、`pureblog-auth` |
| `pureblog-comment` | `pureblog-common`、`pureblog-article`、`pureblog-auth` |
| `pureblog-search` | `pureblog-common`、`pureblog-article`、`pureblog-auth` |
| `pureblog-notification` | `pureblog-common`、`pureblog-auth` |
| `pureblog-stats` | `pureblog-common`、`pureblog-article`、`pureblog-auth` |
| `pureblog-admin` | `pureblog-common`、`pureblog-article`、`pureblog-comment`、`pureblog-stats` |
| `pureblog-app` | 全部子模块 |

禁止反向依赖。例如：`pureblog-article` 不得直接调用 `pureblog-search` 的 Service。

---

## 4. 数据库规则

- 表前缀统一 `pb_`
- 主键统一 `BIGINT UNSIGNED AUTO_INCREMENT`
- 软删除字段统一 `deleted TINYINT(0=未删, 1=已删)`
- 时间字段 `created_at`、`updated_at`，可选 `deleted_at`
- 状态字段统一 `TINYINT`，对应枚举 `getCode()`
- 新增表/字段必须同步更新 `docs/04_数据库设计.sql`
- 复杂查询优先索引优化，不在业务代码里绕 SQL

---

## 5. 后端编码规则

1. Controller 只做参数接收和响应返回
2. 业务逻辑放 `Service` / `impl`
3. 跨表 / 跨中间件逻辑放 `Manager`（如 `ArticleCacheManager`）
4. 基础 CRUD 用 MyBatis-Plus；复杂查询可用 XML
5. 参数校验必须 `@Valid`
6. 出参统一 `ApiResponse<T>`，分页用 `PageResult<T>`
7. 异常用 `BusinessException(ErrorCode.X, msg)`
8. 写接口必须考虑事务边界（`@Transactional`）
9. 禁止直接返回 `DO` 给前端
10. 鉴权信息通过 `LoginUserHolder.get()` 获取

---

## 6. 安全规则

1. 登录用 JWT（accessToken + refreshToken）
2. 后台接口必须校验 `LoginUserHolder.get().isAdmin()`
3. 写接口必须校验当前用户身份
4. **不要在 Service 内吞掉所有异常**：监听器内可吞，业务返回前必须暴露
5. Token 必须经过 `AuthInterceptor` 校验才能被解析

---

## 7. 缓存规则

1. 查询先查缓存，未命中再回源
2. 更新文章后必须 `cacheManager.evictArticleDetail(id)`
3. 计数器走 Redis 原子操作（`INCR`）
4. 用户资料缓存 TTL 30 分钟
5. 验证码 / 黑名单 / Refresh Token 各自独立前缀
6. **Redis 不是主存储**：脏数据不能依赖 Redis

### 7.1 Redis Key 规范
- 全部在 `pureblog-common/.../constant/RedisKey.java` 中定义
- 格式：`pureblog:业务:实体:{id}`（前缀 `pureblog:`）

---

## 8. 事件规则

1. 事件定义必须在 `pureblog-common` 的 `event/` 包
2. 事件名：`XxxEvent`（record 或 class）
3. 监听方法：`onXxx(XxxEvent)`，标 `@EventListener`
4. 监听器中的异常应被捕获并打日志，不应向上抛
5. **不要在主事务内做远程调用**（当前也没有远程调用）
6. 后续如果引入 Kafka，补 Outbox 表（`pb_kafka_outbox` 已建）

---

## 9. 搜索规则

1. 只同步已发布（status=PUBLISHED）文章到 ES
2. 删除 / 下线文章时索引必须同步删除
3. 搜索结果要支持高亮（title / summary / content）
4. 支持分类 / 标签过滤
5. 提供重建索引入口（`POST /api/search/index/rebuild`）

---

## 10. 前端规则

1. 门户端 (`pureblog-portal`) 与管理后台 (`pureblog-admin-web`) 分开维护
2. API 封装在 `src/api/index.ts` 统一 axios 实例
3. 登录态保存 Pinia + localStorage
4. 路由 meta.requiresAuth 标记需登录的页面
5. 表单校验必须完整
6. 页面先保证功能，再优化样式

---

## 11. 输出格式规则

每次让 Cursor 开发一个模块时，必须输出以下内容：

### 11.1 改动说明
- 本次实现了什么
- 涉及哪些文件
- 数据库 / 接口是否有变更

### 11.2 代码输出
- 完整文件内容
- 完整路径

### 11.3 验证说明
- 如何启动
- 如何测试接口
- 如何验证前端页面

### 11.4 下一步建议
- 接下来适合做什么

---

## 12. 禁止事项

Cursor 不允许：

- 引入 Kafka / RocketMQ 等消息中间件（除非明确告知阶段二）
- 引入 MinIO / OSS（除非明确告知阶段二）
- 引入 Spring Cloud、Dubbo、nacos
- 把所有逻辑写到一个 `ServiceImpl`
- 不说明原因就改表结构
- 引用已删除的配置（`KafkaConfig`、`MinioConfig`、`AsyncConfig`、`WebConfig`、`CommonCacheConfig`）
- 改 Spring Security 的过滤器链（鉴权走 `AuthInterceptor`）
- 一次性生成过多无法审阅的代码

---

## 13. 固定开场白建议

你每次都可以先贴这一段给 Cursor：

> 请严格按照本仓库 `docs/06_API接口清单.md` 和 `docs/04_数据库设计.sql` 的实际接口/表结构来开发，不要参考任何已删除的中间件配置。一次只实现一个完整小闭环，输出完整文件路径、代码内容、数据库变更、验证步骤和下一步建议。
