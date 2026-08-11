# 06 API 接口清单

> 说明：以下为第一阶段推荐接口清单。接口风格采用 RESTful + 统一响应结构。

统一响应结构建议：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

---

## 1. 认证模块

### 1.1 注册
- `POST /api/auth/register`

### 1.2 登录
- `POST /api/auth/login`

### 1.3 刷新 token
- `POST /api/auth/refresh-token`

### 1.4 退出登录
- `POST /api/auth/logout`

### 1.5 获取当前用户信息
- `GET /api/auth/me`

---

## 2. 用户模块

### 2.1 获取作者主页
- `GET /api/users/{userId}/profile`

### 2.2 更新个人资料
- `PUT /api/users/profile`

### 2.3 关注作者
- `POST /api/users/{userId}/follow`

### 2.4 取消关注作者
- `DELETE /api/users/{userId}/follow`

### 2.5 查询我的关注列表
- `GET /api/users/following`

### 2.6 查询我的粉丝列表
- `GET /api/users/followers`

---

## 3. 文章模块（前台）

### 3.1 首页文章列表
- `GET /api/articles`

支持参数：
- `pageNum`
- `pageSize`
- `keyword`
- `categoryId`
- `tagId`
- `authorId`
- `sortBy=latest|hot`

### 3.2 文章详情
- `GET /api/articles/{articleId}`

### 3.3 热门文章
- `GET /api/articles/hot`

### 3.4 最新文章
- `GET /api/articles/latest`

### 3.5 作者文章列表
- `GET /api/users/{userId}/articles`

---

## 4. 文章模块（作者后台）

### 4.1 新增文章
- `POST /api/author/articles`

### 4.2 保存草稿
- `POST /api/author/articles/draft`

### 4.3 更新文章
- `PUT /api/author/articles/{articleId}`

### 4.4 发布文章
- `POST /api/author/articles/{articleId}/publish`

### 4.5 下线文章
- `POST /api/author/articles/{articleId}/offline`

### 4.6 删除文章
- `DELETE /api/author/articles/{articleId}`

### 4.7 查询我的文章分页
- `GET /api/author/articles`

### 4.8 查询文章版本列表
- `GET /api/author/articles/{articleId}/versions`

### 4.9 查询文章指定版本
- `GET /api/author/articles/{articleId}/versions/{versionNo}`

---

## 5. 分类与标签

### 5.1 查询分类列表
- `GET /api/categories`

### 5.2 查询标签列表
- `GET /api/tags`

### 5.3 新增分类（管理员）
- `POST /api/admin/categories`

### 5.4 更新分类（管理员）
- `PUT /api/admin/categories/{id}`

### 5.5 删除分类（管理员）
- `DELETE /api/admin/categories/{id}`

### 5.6 新增标签（管理员）
- `POST /api/admin/tags`

### 5.7 更新标签（管理员）
- `PUT /api/admin/tags/{id}`

### 5.8 删除标签（管理员）
- `DELETE /api/admin/tags/{id}`

---

## 6. 评论模块

### 6.1 发表评论
- `POST /api/articles/{articleId}/comments`

### 6.2 回复评论
- `POST /api/comments/{commentId}/reply`

### 6.3 查询文章评论树
- `GET /api/articles/{articleId}/comments`

### 6.4 删除我的评论
- `DELETE /api/comments/{commentId}`

### 6.5 评论审核分页（管理员）
- `GET /api/admin/comments`

参数：
- `status`
- `articleId`
- `userId`

### 6.6 审核通过
- `POST /api/admin/comments/{commentId}/approve`

### 6.7 审核拒绝
- `POST /api/admin/comments/{commentId}/reject`

---

## 7. 搜索模块

### 7.1 全文搜索
- `GET /api/search/articles`

参数：
- `keyword`
- `pageNum`
- `pageSize`
- `categoryId`
- `tagId`
- `sortBy`

### 7.2 重建全文索引（管理员）
- `POST /api/admin/search/rebuild`

### 7.3 查询索引重建任务状态（管理员）
- `GET /api/admin/search/rebuild/tasks/{taskId}`

---

## 8. 通知模块

### 8.1 我的通知列表
- `GET /api/notifications`

### 8.2 标记已读
- `POST /api/notifications/{id}/read`

### 8.3 全部已读
- `POST /api/notifications/read-all`

### 8.4 未读数量
- `GET /api/notifications/unread-count`

---

## 9. 统计模块

### 9.1 作者仪表盘
- `GET /api/author/dashboard`

### 9.2 管理后台仪表盘
- `GET /api/admin/dashboard`

### 9.3 文章统计详情
- `GET /api/author/articles/{articleId}/stats`

---

## 10. 文件上传模块

### 10.1 上传封面图
- `POST /api/files/upload/cover`

### 10.2 上传正文图片
- `POST /api/files/upload/content-image`

---

## 11. 后台管理模块

### 11.1 用户分页
- `GET /api/admin/users`

### 11.2 用户状态修改
- `POST /api/admin/users/{id}/status`

### 11.3 角色配置
- `PUT /api/admin/users/{id}/roles`

### 11.4 文章管理分页
- `GET /api/admin/articles`

### 11.5 强制下线文章
- `POST /api/admin/articles/{id}/offline`

---

## 12. 接口开发要求

- 所有写接口都要做参数校验
- 所有鉴权接口统一从 SecurityContext 获取当前用户
- 列表接口统一支持分页
- 后台接口统一 RBAC 权限控制
- 出参与入参分离，不能直接返回 DO
- 错误码统一管理

---

## 13. 推荐实现顺序

### 第一批
- auth
- article 基础 CRUD
- category/tag
- article 列表/详情

### 第二批
- comment
- notification
- stats

### 第三批
- search
- rebuild
- admin dashboard

### 第四批
- upload
- 限流
- 审计日志
