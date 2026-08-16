import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory('/admin'),
  routes: [
    { path: '/login', name: 'login', component: () => import('@/views/Login.vue') },
    {
      path: '/',
      component: () => import('@/components/AdminLayout.vue'),
      redirect: '/dashboard',
      children: [
        { path: 'dashboard', name: 'dashboard', component: () => import('@/views/Dashboard.vue') },
        { path: 'article/list', name: 'article-list', component: () => import('@/views/article/ArticleList.vue') },
        { path: 'article/edit', name: 'article-edit', component: () => import('@/views/article/ArticleEdit.vue') },
        { path: 'tree/manage', name: 'tree-manage', component: () => import('@/views/tree/TreeManage.vue') },
        { path: 'tree/node/:treeId', name: 'tree-node-manage', component: () => import('@/views/tree/TreeNodeManage.vue') },
        { path: 'comment/audit', name: 'comment-audit', component: () => import('@/views/comment/CommentAudit.vue') },
        { path: 'user/manage', name: 'user-manage', component: () => import('@/views/user/UserManage.vue') },
        { path: 'category/manage', name: 'category-manage', component: () => import('@/views/category/CategoryManage.vue') },
        { path: 'tag/manage', name: 'tag-manage', component: () => import('@/views/tag/TagManage.vue') }
      ]
    }
  ]
})

router.beforeEach((to) => {
  if (to.name === 'login') return
  const token = localStorage.getItem('admin_token')
  if (!token) {
    return { name: 'login' }
  }
})

export default router
