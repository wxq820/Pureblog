# 06 API 接口清单

> 当前所有 HTTP 接口的清单。统一前缀 `/api`，统一响应结构 `ApiResponse<T>`：
> ```json
> { "code": 0, "message": "success", "data": {} }
> ```
> 分页响应 `PageResult<T>`：
> ```json
> { "records": [], "total": 0, "page": 1, "size": 10, "totalPages": 0 }
> ```

所有带鉴权要求的接口，需要在 Header 携带 `Authorization: Bearer <accessToken>`，由 `AuthInterceptor` 校验。

---

## 0. 目录树模块（Tree）

> 与 `pb_category`（分类）是两个独立维度：分类用于「文章归类」，目录树用于「按技术栈/知识图谱组织」。文章**必须**挂到目录树的叶子节点才能保存。

### 0.1 公开端点（前台）

#### 0.1.1 列出所有启用目录树
- `GET /api/tree/public/list`
- 公开

#### 0.1.2 获取某棵树的完整嵌套结构
- `GET /api/tree/public/{code}`
- Path：`code` 为目录树唯一编码（如 `java`）
- 公开

### 0.2 后台端点：树主表（需管理员）

#### 0.2.1 列出所有树
- `GET /api/admin/tree/list`
- 需管理员

#### 0.2.2 新建树
- `POST /api/admin/tree/create`
- Body：`{code, name, description?, coverColor?, sortOrder?}`
- 需管理员

#### 0.2.3 更新树
- `PUT /api/admin/tree/update`
- Body：`{id, name?, description?, coverColor?, sortOrder?, status?}`
- 需管理员

#### 0.2.4 更新树状态
- `POST /api/admin/tree/status/{id}?status=1|0`
- 需管理员

#### 0.2.5 删除树（仅当节点数 ≤ 1）
- `DELETE /api/admin/tree/{id}`
- 需管理员

### 0.3 后台端点：树节点（需管理员）

#### 0.3.1 列出某棵树的所有节点
- `GET /api/admin/tree-node/list/{treeId}`
- 公开（白名单未拦截）

#### 0.3.2 新建节点
- `POST /api/admin/tree-node/create`
- Body：`{treeId, parentId, name, color?, sortOrder?}`
- 需管理员

#### 0.3.3 更新节点
- `PUT /api/admin/tree-node/update`
- Body：`{id, name?, color?, sortOrder?}`
- 需管理员

#### 0.3.4 移动节点
- `POST /api/admin/tree-node/move`
- Body：`{id, newParentId, newSortOrder?}`
- 需管理员
- 不允许把节点移到自己的后代里；只允许同树内移动

#### 0.3.5 删除节点（无子节点 + 无文章）
- `DELETE /api/admin/tree-node/{id}`
- 需管理员

---

## 1. 认证模块

### 1.1 登录
- `POST /api/auth/login`
- Body: `{username, password, captchaKey?, captchaCode?}`
- 公开

### 1.2 注册
- `POST /api/auth/register`
- Body: `{username, password, email, nickname?}`
- 公开
- **当前默认不在前端引导**（个人博客定位，新账号直接写库 `pb_user` + BCrypt）

### 1.3 刷新 Token
- `POST /api/auth/refresh`
- Body: `{refreshToken}`
- 公开

### 1.4 退出登录
- `POST /api/auth/logout`
- 需登录

### 1.5 获取图形验证码 Key
- `GET /api/auth/captcha`
- 返回 `{key}`
- 公开

---

## 2. 用户模块

### 2.1 当前用户
- `GET /api/user/me`
- 需登录

### 2.2 他人主页
- `GET /api/user/public/{userId}`
- 公开

### 2.3 修改个人资料
- `PUT /api/user/profile`
- Body: `{nickname?, bio?, avatarUrl?}`
- 需登录

### 2.4 关注
- `POST /api/user/follow/{userId}`
- 需登录

### 2.5 取消关注
- `DELETE /api/user/follow/{userId}`
- 需登录

### 2.6 粉丝列表
- `GET /api/user/followers/{userId}?page=1&size=20`
- 公开

### 2.7 关注列表
- `GET /api/user/following/{userId}?page=1&size=20`
- 公开

---

## 3. 文章模块（前台）

### 3.1 文章列表
- `GET /api/article/list?page=1&size=10&keyword=&categoryId=&tagId=&treeId=&treeNodeId=&sortBy=&sortOrder=`
- `sortBy`: `publishedAt`（默认） | `viewCount` | `likeCount` | `commentCount`
- `sortOrder`: `asc` | `desc`（默认）
- 排序优先级：`is_top` 置顶优先 → 按 `sortBy` 指定的字段 → `published_at`
- `treeId` / `treeNodeId` 为可选过滤项；两者都给时以 `treeNodeId` 为准
- 公开

### 3.2 文章详情
- `GET /api/article/public/{id}`
- 公开

### 3.3 热门文章
- `GET /api/article/hot?limit=10`
- 公开

### 3.4 精选文章
- `GET /api/article/featured?limit=5`
- 公开

### 3.5 作者文章
- `GET /api/article/author/{authorId}?page=1&size=10`
- 公开

### 3.6 分类列表
- `GET /api/category/list`
- 公开

### 3.7 标签列表
- `GET /api/tag/list`
- 公开

---

## 4. 文章模块（作者）

### 4.1 创建草稿
- `POST /api/article/create`
- Body: `{title, summary, content, htmlContent, categoryId, tagIds?, coverUrl?}`
- 需登录

### 4.2 更新文章
- `PUT /api/article/update`
- Body: `{id, title, summary, content, htmlContent, categoryId, tagIds?, coverUrl?}`
- 需登录，且为作者本人

### 4.3 发布
- `POST /api/article/publish`
- Body: `{id, isFeatured?, isTop?}`
- 需登录

### 4.4 下线
- `POST /api/article/offline/{id}`
- 需登录，且为作者本人

### 4.5 删除
- `DELETE /api/article/{id}`
- 需登录，且为作者本人

### 4.6 触发单个文章索引重建
- **已下线** —— 搜索改用 MySQL `LIKE` 后不再需要索引重建，该接口随 `ArticleService.rebuildSearchIndex` 一并移除。

---

## 5. 评论模块

### 5.1 发表评论
- `POST /api/comment/create`
- Body: `{articleId, content, parentId?, replyToId?, replyToUid?}`
- 需登录

### 5.2 删除评论
- `DELETE /api/comment/{id}`
- 需登录，且为评论作者

### 5.3 文章评论树
- `GET /api/comment/article/{articleId}?page=1&size=20`
- 公开

### 5.4 文章点赞
- `POST /api/article/like/{articleId}`
- 需登录

### 5.5 取消文章点赞
- `DELETE /api/article/like/{articleId}`
- 需登录

### 5.6 文章收藏
- `POST /api/article/collect/{articleId}`
- 需登录

### 5.7 取消文章收藏
- `DELETE /api/article/collect/{articleId}`
- 需登录

### 5.8 文章统计（点赞/收藏/评论 + 个人是否已点赞/收藏）
- `GET /api/article/stats/{articleId}`
- 公开

---

## 6. 搜索模块

### 6.1 全文搜索
- `GET /api/search?keyword=&categoryId=&tagId=&sortBy=&sortOrder=&page=1&size=10`
  - `keyword`: 匹配范围为 标题 OR 摘要 OR 正文 OR 标签名
  - `sortBy`: `relevance` | `viewCount` | `likeCount` | `publishedAt`
  - `sortOrder`: `asc` | `desc`
- 公开

**响应结构**（`data` 字段）：

```json
{
  "articles": [
    {
      "articleId": 1,
      "title": "标题中带 <em>高亮</em> 的标题",
      "summary": "文章摘要（原文，不转义）",
      "authorName": "作者昵称",
      "authorAvatar": null,
      "authorId": 1,
      "categoryName": "技术",
      "tagNames": ["Java", "Redis"],
      "viewCount": 100,
      "likeCount": 10,
      "publishedAt": "2026-10-28 10:00:00",
      "highlights": ["正文命中片段，带 <em>高亮</em>"]
    }
  ],
  "total": 1,
  "page": 1,
  "size": 10,
  "totalPages": 1,
  "tookMs": 12
}
```

> `title` 与 `highlights` 由服务端做 HTML 转义后插入 `<em>` 标签（前端用 `v-html` 渲染）；
> `summary` 不转义（前端用 `{{ }}` 插值渲染，转义会导致双重转义显示成 `&amp;lt;`）。

### 6.2 索引管理
- **已下线** —— `POST /api/search/index/create` 与 `POST /api/search/index/rebuild` 随索引方案一起移除。
  搜索走 MySQL `LIKE`，无索引需要创建或重建。

---

## 7. 通知模块

### 7.1 我的通知
- `GET /api/notification/list?page=1&size=20`
- 需登录

### 7.2 标记已读
- `POST /api/notification/read/{id}`
- 需登录

### 7.3 全部已读
- `POST /api/notification/read/all`
- 需登录

### 7.4 未读数
- `GET /api/notification/unread/count`
- 需登录

---

## 8. 统计模块

### 8.1 仪表盘
- `GET /api/stats/dashboard`
- 公开（生产应加管理员校验）

### 8.2 热门文章
- `GET /api/stats/hot?days=7&limit=10`
- 公开

### 8.3 作者热度
- `GET /api/stats/author/{authorId}?limit=10`
- 公开

### 8.4 记录 PV
- `POST /api/stats/pv`
- Body: `{articleId, ip?}`
- 公开

---

## 9. 管理后台

### 9.1 仪表盘
- `GET /api/admin/dashboard`
- 需登录，且为管理员

### 9.2 用户列表
- `GET /api/admin/user/list?page=1&size=20&keyword=`
- 需管理员

### 9.3 修改用户
- `PUT /api/admin/user/update`
- Body: `{userId, role?, status?}`
- 需管理员

### 9.4 禁用用户
- `POST /api/admin/user/disable/{userId}`
- 需管理员

### 9.5 文章列表
- `GET /api/admin/article/list?page=1&size=20&keyword=&authorId=&status=`
- 需管理员

### 9.6 下架文章
- `POST /api/admin/article/offline/{id}`
- 需管理员

### 9.7 删除文章
- `DELETE /api/admin/article/{id}`
- 需管理员

### 9.8 待审核评论
- `GET /api/admin/comment/pending?page=1&size=20`
- 需管理员

### 9.9 评论审核
- `POST /api/admin/comment/audit/{id}?approve=true|false`
- 需管理员

### 9.10 删除评论
- `DELETE /api/admin/comment/{id}`
- 需管理员

### 9.11 创建分类
- `POST /api/admin/category/create`
- Body: `{name, slug, description?, sortOrder?}`
- 需管理员

### 9.12 更新分类
- `PUT /api/admin/category/update`
- Body: `{id, name?, slug?, description?, sortOrder?}`
- 需管理员

### 9.13 删除分类
- `DELETE /api/admin/category/{id}`
- 需管理员

### 9.14 创建标签
- `POST /api/admin/tag/create`
- Body: `{name, slug}`
- 需管理员

### 9.15 更新标签
- `PUT /api/admin/tag/update`
- Body: `{id, name?, slug?}`
- 需管理员

### 9.16 删除标签
- `DELETE /api/admin/tag/{id}`
- 需管理员

### 9.17 小工具管理
- `GET /api/admin/widget/list` —— 列出全部小工具（含禁用）
- `POST /api/admin/widget/create` —— Body：`{code, name, description?, icon?, iconColor?, path, sortOrder?, status?}`
- `PUT /api/admin/widget/update` —— Body：`{id, name?, description?, icon?, iconColor?, path?, sortOrder?, status?}`
- `POST /api/admin/widget/status/{id}?status=1|0`
- `DELETE /api/admin/widget/{id}`
- 需管理员
- `icon` 为 Element Plus Icons 图标名（如 `EditPen` / `Wallet`），取值见 `frontend/pureblog-portal/src/constants/widgetIcons.ts`

---

## 10. 小工具模块

> 小工具是个人使用场景，**全部接口均需登录**（未登录返回 401）。
> 前端首页仅在登录后渲染「小工具」卡片，`/widget/:code` 路由也走 `requiresAuth` 守卫。

### 10.1 小工具入口
- `GET /api/widget/list` —— 列出所有启用的小工具（按 `click_count` 降序）
- `POST /api/widget/click` —— Body：`{widgetId, deviceId}`，记 1 次点击（按设备+日去重）
- 需登录

### 10.2 记账（/api/ledger）
- `GET /api/ledger/dict?type=1|2` —— `type=1` 平台字典，`type=2` 项目字典
- `POST /api/ledger/dict` —— Body：`{type, name, color?, icon?}`
- `GET /api/ledger/record?deviceId=&limit=100` —— 按设备查最近记录
- `POST /api/ledger/record` —— Body：`{recordDate, platformDictId, itemDictId?, itemText?, amount, deviceId}`
- 需登录
- 备注：记账数据目前按 `deviceId`（前端 localStorage 匿名 ID）隔离，登录只是访问门槛

### 10.3 身家（/api/widget/networth）
- `GET /api/widget/networth/dashboard`
- `GET /api/widget/networth/snapshot/list`
- `GET /api/widget/networth/snapshot/{id}/entries`
- `POST /api/widget/networth/snapshot/save`
- `DELETE /api/widget/networth/snapshot/{id}`
- 需登录

### 10.4 回本之路（/api/widget/recovery）
- `GET /api/widget/recovery/dashboard`
- `POST /api/widget/recovery/position/save`
- `POST /api/widget/recovery/value/save`
- `DELETE /api/widget/recovery/position/{id}`
- 需登录

### 10.5 投资表（/api/widget/fund）
- `GET /api/widget/fund/dashboard`
- `POST /api/widget/fund/save`
- `POST /api/widget/fund/buy/save`
- `POST /api/widget/fund/nav/save`
- `DELETE /api/widget/fund/buy/{id}`
- `DELETE /api/widget/fund/{id}`
- 需登录

---

## 11. 公共

### 11.1 健康检查
- `GET /actuator/health`
- 公开

### 11.2 应用信息
- `GET /actuator/info`
- 公开

---

## 12. 接口开发约束

- 所有写接口必须 `@Valid` 校验
- 列表接口统一分页（`page` 从 1 起）
- 出参与入参严格分离，**禁止直接返回 `DO`**
- 错误码统一在 `ErrorCode` 枚举中
- 鉴权通过 `LoginUserHolder.get()` 获取当前用户，宁可早抛 `UNAUTHORIZED`
