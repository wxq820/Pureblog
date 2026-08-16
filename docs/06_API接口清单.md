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

## 1. 认证模块

### 1.1 登录
- `POST /api/auth/login`
- Body: `{username, password, captchaKey?, captchaCode?}`
- 公开

### 1.2 注册
- `POST /api/auth/register`
- Body: `{username, password, email, nickname?}`
- 公开

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
- `GET /api/article/list?page=1&size=10&keyword=&categoryId=&tagId=&sortBy=&sortOrder=`
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
- `POST /api/article/rebuild-index/{id}`
- 需登录

> 当前 `ArticleServiceImpl.rebuildSearchIndex` 已被声明，但路由未在 `ArticleController` 暴露。可以通过调用 `ArticleEventProducer.sendArticleEvent(REBUILD_INDEX)` 实现单条重建。

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
  - `sortBy`: `relevance` | `viewCount` | `likeCount` | `publishedAt`
  - `sortOrder`: `asc` | `desc`
- 公开

### 6.2 创建索引
- `POST /api/search/index/create`
- 公开（生产环境应加管理员校验）

### 6.3 全量重建索引
- `POST /api/search/index/rebuild`
- 公开（生产环境应加管理员校验）

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

---

## 10. 公共

### 10.1 健康检查
- `GET /actuator/health`
- 公开

### 10.2 应用信息
- `GET /actuator/info`
- 公开

---

## 11. 接口开发约束

- 所有写接口必须 `@Valid` 校验
- 列表接口统一分页（`page` 从 1 起）
- 出参与入参严格分离，**禁止直接返回 `DO`**
- 错误码统一在 `ErrorCode` 枚举中
- 鉴权通过 `LoginUserHolder.get()` 获取当前用户，宁可早抛 `UNAUTHORIZED`
