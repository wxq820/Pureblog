# 10 Cursor 开发 Prompt 模板

下面这些 Prompt 可以直接复制给 Cursor。

---

## 1. 初始化后端工程

```text
请基于 docs/00_项目总览.md、docs/01_技术栈与技术背景.md、docs/02_系统架构设计.md、docs/03_项目目录建议.md，先初始化后端 Spring Boot 3 多模块工程。

要求：
1. 使用 Java 17、Maven、多模块结构
2. 生成 backend 下的基础工程目录
3. 包含 common、app、auth、user、article、comment、search、notification、stats 模块
4. 接入基础配置：MySQL、Redis、Kafka、Elasticsearch 的 application-dev.yml 模板
5. 实现统一响应结构、统一异常处理、基础日志配置
6. 输出完整文件路径和代码
7. 不要实现具体业务接口
```

---

## 2. 初始化前端工程

```text
请基于 docs/03_项目目录建议.md 初始化 frontend 下的两个前端项目：

1. pureblog-portal
2. pureblog-admin-web

要求：
- 使用 Vue3 + TypeScript + Vite + Pinia + Vue Router
- 管理台使用 Element Plus
- 门户端实现基础 Layout
- 输出完整文件路径和代码
- 暂时不用接业务接口，只搭建骨架
```

---

## 3. 实现用户与登录模块

```text
请基于 docs/04_数据库设计.sql 和 docs/06_API接口清单.md，实现后端 auth + user 模块的第一版。

范围：
- 用户注册
- 登录
- 获取当前用户信息
- 关注作者
- 取消关注作者
- 作者主页查询

要求：
1. 生成 entity、mapper、service、controller、dto、vo
2. 使用 JWT + Spring Security
3. 参数校验完整
4. 输出完整文件路径和代码
5. 给出数据库是否需要补充变更
6. 给出 Postman 验证步骤
```

---

## 4. 实现文章模块

```text
请基于 docs/04_数据库设计.sql、docs/05_核心业务流程.md、docs/06_API接口清单.md，实现 article 模块第一版。

范围：
- 分类 CRUD
- 标签 CRUD
- 文章新增
- 保存草稿
- 更新文章
- 发布文章
- 下线文章
- 删除文章
- 文章列表
- 文章详情
- 文章版本快照

要求：
1. 严格按照文章元数据和正文分表实现
2. 输出 entity、mapper、xml（如需要）、service、manager、controller、dto、vo、convert
3. 发布与更新时维护文章版本
4. 不要先实现 Kafka 和 ES
5. 文章详情加 Redis 缓存，更新后删除缓存
6. 给出接口验证步骤
```

---

## 5. 实现评论模块

```text
请基于 docs/04_数据库设计.sql、docs/05_核心业务流程.md、docs/06_API接口清单.md，实现评论与审核模块。

范围：
- 发表评论
- 回复评论
- 查询评论树
- 删除评论
- 管理台评论审核分页
- 审核通过
- 审核拒绝

要求：
1. 评论先可靠落库，不允许先写内存队列
2. 支持 parent_id、root_id、reply_user_id 结构
3. 输出完整文件路径和代码
4. 给出前台与后台查询示例
```

---

## 6. 实现 Kafka 事件

```text
请基于 docs/05_核心业务流程.md 和 docs/08_非功能需求与技术亮点.md，为 article 发布/更新/删除、comment 创建实现 Kafka 事件链路。

要求：
1. 定义事件对象
2. 实现 Producer
3. 实现 Consumer
4. Consumer 内先做日志记录和伪业务处理
5. 考虑消费幂等，使用 pb_message_consume_record
6. 输出完整文件路径和代码
7. 不要修改已经实现好的业务接口风格
```

---

## 7. 实现 Elasticsearch 搜索

```text
请基于 docs/05_核心业务流程.md 与 docs/06_API接口清单.md，实现 Elasticsearch 搜索模块。

要求：
1. 建立文章索引模型
2. 支持标题、摘要、正文、标签搜索
3. 支持高亮
4. 支持分类/标签过滤
5. 支持最新/最热排序
6. 实现索引同步 consumer
7. 实现重建索引接口
8. 输出完整文件路径和代码
```

---

## 8. 实现热榜与统计

```text
请基于 docs/04_数据库设计.sql、docs/05_核心业务流程.md、docs/08_非功能需求与技术亮点.md，实现统计模块。

范围：
- 文章浏览量计数
- 热度分计算
- 热门文章缓存
- 作者仪表盘
- 管理台仪表盘

要求：
1. 使用 Redis 原子计数
2. 周期性将计数刷新到 MySQL
3. 热榜结果写 Redis
4. 输出完整代码与定时任务实现
```

---

## 9. 实现管理后台页面

```text
请基于 docs/06_API接口清单.md 和 docs/07_开发任务拆解.md，实现 pureblog-admin-web 的以下页面：

- 登录页
- 仪表盘
- 分类管理
- 标签管理
- 文章管理
- 评论审核
- 用户管理

要求：
1. 使用 Vue3 + TS + Element Plus
2. API 单独封装
3. 表格、分页、表单校验完整
4. 输出完整文件路径和代码
```

---

## 10. 实现门户端页面

```text
请基于 docs/06_API接口清单.md，实现 pureblog-portal 的以下页面：

- 首页文章列表
- 搜索页
- 文章详情页
- 作者主页
- 登录页
- 通知页

要求：
1. 使用 Vue3 + TS
2. 页面先保证功能，不追求复杂视觉
3. 输出完整文件路径和代码
```

---

## 11. 做一次代码审查

```text
请作为资深 Java 架构师，按照 docs/09_Cursor协作规则.md 对当前项目代码进行审查。

重点检查：
1. 是否违反模块边界
2. 是否有 controller 直连 mapper
3. 是否有缓存一致性遗漏
4. 是否有消息幂等遗漏
5. 是否有 DTO/VO/DO 混用
6. 是否有明显的空指针或事务边界问题

输出格式：
- 问题位置
- 原问题
- 修改建议
- 修改后的代码示例
```

---

## 12. 生成 Docker Compose

```text
请基于本项目技术栈生成本地开发用 docker-compose.yml。

要求：
1. 包含 mysql、redis、kafka、zookeeper、elasticsearch、minio
2. 端口配置清晰
3. 提供默认用户名、密码、挂载目录
4. 给出启动顺序和验证方式
```
