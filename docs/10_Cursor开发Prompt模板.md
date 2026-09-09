# 10 Cursor 开发 Prompt 模板

下面这些 Prompt 可以直接复制给 Cursor。所有 Prompt 假设 Cursor 已经读取 `docs/09_Cursor协作规则.md` 和 `.cursor/rules/docs-sync.mdc`。

---

## 1. 修复某个 bug

```text
请在我的 Spring Boot 3 工程里修复 [问题描述]。

参考文档：
- 接口：docs/06_API接口清单.md
- 表结构：docs/04_数据库设计.sql
- 协作规则：docs/09_Cursor协作规则.md
- 文档同步规则：.cursor/rules/docs-sync.mdc

要求：
1. 先定位问题代码，给出文件路径和行号
2. 解释根因
3. 给出修复代码（完整文件或 diff）
4. 给出本地验证步骤
5. 给出下一步建议
6. 如有接口/表结构变更，列出对应 docs/ 同步项
```

---

## 2. 新增后台接口

```text
请在 pureblog-admin 模块新增一个接口：

需求：
- 功能：[业务描述]
- 入参：[DTO 字段]
- 出参：[VO 字段]
- 权限：管理员（LoginUserHolder.get().isAdmin()）

参考文档：
- docs/06_API接口清单.md 第 9 节
- docs/09_Cursor协作规则.md 第 5 节

要求：
1. 给出 Service / Controller / DTO / VO 完整代码
2. 给出单元/手动验证步骤
3. 不要修改其他模块
4. 同步更新 docs/06_API接口清单.md
```

---

## 3. 新增 Redis 缓存

```text
请在 [业务模块] 给 [接口路径] 增加 Redis 缓存。

参考：
- 现有实现：ArticleCacheManager
- 规则：docs/09_Cursor协作规则.md 第 7 节

要求：
1. 在 RedisKey 中定义新 key
2. 提供 evict 方法保证更新后失效
3. 缓存粒度、TTL 给出理由
4. 给出降级策略（缓存挂掉时回源 MySQL）
```

---

## 4. 新增事件

```text
请新增一个进程内事件 [EventName]。

参考：
- 现有事件：com.pureblog.common.event.CommentCreatedEvent
- 规则：docs/09_Cursor协作规则.md 第 8 节

要求：
1. Event 定义在 pureblog-common 的 event 包
2. 提供发布方用法（哪个 Service 在什么时机调用）
3. 提供至少一个 @EventListener 监听器
4. 监听器内 try-catch，避免影响主事务
5. 给出测试步骤
```

---

## 5. 重构模块

```text
请重构 [模块名]，把 [职责] 拆到独立的 Manager / Service。

约束：
- 不改变现有接口签名
- 不改变数据库表结构
- 单元测试通过

要求：
1. 重构前先给出改造方案
2. 重构后给出 diff
3. 给出验证命令
```

---

## 6. 接入文件存储（OSS / S3）

```text
请把当前的文件上传从本地 ./uploads/ 切换到 [OSS/S3]。

前置：
- 表结构已建：pb_file（见 docs/04_数据库设计.sql）
- 当前实现：UploadController（仅本地存储）

要求：
1. 替换 UploadController 的存储实现，接口签名不变
2. 配置项放到 application.yml
3. 给出本地验证步骤（仍可在 dev 环境下走本地存储）
4. 同步更新 docs/01、docs/06
```

---

## 7. 加入限流

```text
请为评论接口加入限流（按用户维度，5 秒内最多 3 次）。

参考：
- Redis 工具：RedisTemplate
- 规则：docs/09_Cursor协作规则.md 第 6 节

要求：
1. 用 Redis 原子命令 + 滑动窗口
2. 限流 key：pureblog:rate:comment:{userId}
3. 超限返回 RATE_LIMIT_EXCEEDED
4. 给出验证方法
```

---

## 8. 加入 ES 全量重建

```text
请实现定时任务：每天凌晨 3 点全量重建 ES 索引。

参考：
- SearchServiceImpl.rebuildAllIndex
- 规则：docs/09_Cursor协作规则.md 第 9 节

要求：
1. 在 StatsServiceImpl 或新 Schedule 类中加 @Scheduled(cron = "0 0 3 * * ?")
2. 调用 searchService.rebuildAllIndex
3. 失败日志要详细
4. 给出验证方法
```

---

## 9. 启动后端

```text
请帮我启动后端。具体步骤：
1. 启动 MySQL 与 Redis（docker compose -f deploy/docker-compose.dev.yml up -d）
2. 启动 Elasticsearch（参考 README）
3. 初始化数据库（mysql -uroot -p123456 < scripts/schema.sql）
4. 初始化目录树（mysql -uroot -p123456 pureblog < backend/sql/init_tree.sql）
5. 在 backend 目录执行 mvn -pl pureblog-app -am spring-boot:run

请给出预期输出与可能的错误处理。
```

---

## 10. 做一次代码审查

```text
请作为资深 Java 架构师，按 docs/09_Cursor协作规则.md 对当前项目代码进行审查。

重点检查：
1. 是否违反模块依赖边界
2. 是否有 controller 直连 mapper
3. 是否有缓存一致性遗漏
4. 是否有事件监听器内抛出异常
5. 是否有 DTO/VO/DO 混用
6. 是否有事务边界问题
7. 是否有接口重复 / 死代码
8. 鉴权是否走 LoginUserHolder（而不是 SecurityContext）
9. 文章保存是否调用 treeService.assertIsLeaf

输出格式：
- 问题位置
- 原问题
- 修改建议
- 修改后的代码示例
```

---

## 11. 性能压测

```text
请基于 k6 或 wrk 写一份压测脚本，模拟 100 并发访问文章详情接口。

要求：
1. 包含冷启动（缓存未命中）和热启动（缓存命中）两阶段
2. 输出 P50 / P95 / P99 延迟
3. 给出瓶颈分析建议
```

---

## 12. 接入 Prometheus

```text
请为项目集成 Micrometer + Prometheus。

要求：
1. 引入 micrometer-registry-prometheus 依赖
2. 暴露 /actuator/prometheus
3. 给关键 Service 方法加 @Timed
4. 给出 Grafana 看板字段建议
5. 同步更新 docs/01、docs/08
```

---

## 13. 调整接口

```text
请修改 /api/article/list 接口，增加一个参数：tagIds（多标签 AND 过滤）。

要求：
1. 同步更新 API 文档 docs/06_API接口清单.md
2. 给出 ArticleQueryDTO 的新字段
3. Service 中构造 wrapper.eq(ArticleDO::xxx, ...)
4. 给出验证步骤
```

---

## 14. 加一条事务一致性保障

```text
发布文章时，需要同时：
1. 写 pb_article
2. 写 pb_article_content
3. 写 pb_article_tag
4. 更新 user.article_count
5. 更新 category.article_count
6. treeService.incrementArticleCount(treeNodeId)

要求：
1. 用 @Transactional 包裹
2. rollbackFor = Exception.class
3. 失败日志详细
4. 验证：故意制造异常，确认全部回滚
```

---

## 15. 接通树节点 → 文章列表

```text
请把首页目录树选中后的「按节点过滤文章列表」接通。

当前状态：
- 后端：ArticleServiceImpl.getArticleList 已经支持 treeId / treeNodeId 过滤
- 前端：SkillTree 选中 depth>=2 节点后，期望 HomePage 右侧列表按该节点过滤
- 前端路由：?nodeId=X&treeId=Y

要求：
1. SkillTree.vue onClick 命中 depth>=2 节点时 router.push({ query: { nodeId, treeId } })
2. HomePage.vue watch route.query.nodeId 清空 articles + 重置 page + 调 articleApi.getList({ treeNodeId, treeId, page: 1 })
3. 顶部展示当前选中节点路径
4. 同步更新 docs/02、docs/05、docs/06
5. 给出验证步骤
```

---

## 16. 修排序 bug

```text
请修 ArticleServiceImpl.getArticleList 的排序 bug。

当前 bug：
- 代码用 switch 解析了 sortField 变量，但从来没把它传给 LambdaQueryWrapper
- wrapper 始终 orderByDesc(isTop).orderByDesc(viewCount).orderByDesc(publishedAt)，sortBy 参数失效

要求：
1. 修复排序：isTop 置顶优先 → 按 sortBy 指定的字段（viewCount/likeCount/commentCount/publishedAt）→ asc/desc
2. 不要改其他逻辑
3. 同步更新 docs/02、docs/06、docs/10 的对应章节
4. 给出验证方法
```
