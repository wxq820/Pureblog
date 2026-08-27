git add -A
git commit -m "feat: 简化header并合并管理布局到默认布局
- AppHeader: 未登录只显示logo+登录按钮,登录后显示logo+5个tab+写文章+头像菜单
- 删除独立AdminLayout,管理路由直接在DefaultLayout下渲染
- 移除comment/audit和user/manage路由
- 删除旧的pureblog-admin-web项目,功能合并到portal"
git push
