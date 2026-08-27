import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/layouts/DefaultLayout.vue'),
      children: [
        { path: '', name: 'home', component: () => import('@/views/home/HomePage.vue') },
        { path: 'article/:id', name: 'article-detail', component: () => import('@/views/article/ArticleDetail.vue') },
        { path: 'article/:id/edit', name: 'article-editor', component: () => import('@/views/article/ArticleEditor.vue'), meta: { requiresAuth: true } },
        { path: 'search', name: 'search', component: () => import('@/views/search/SearchPage.vue') },
        { path: 'user/:id', name: 'user-profile', component: () => import('@/views/user/UserProfile.vue') },
        { path: 'notifications', name: 'notifications', component: () => import('@/views/notification/NotificationList.vue'), meta: { requiresAuth: true } },
        // 管理路由（登录后才可见）
        { path: 'admin', name: 'admin-dashboard', component: () => import('@/views/admin/Dashboard.vue'), meta: { requiresAuth: true } },
        { path: 'admin/article/list', name: 'article-list', component: () => import('@/views/admin/ArticleManage.vue'), meta: { requiresAuth: true } },
        { path: 'admin/category/manage', name: 'category-manage', component: () => import('@/views/admin/CategoryManage.vue'), meta: { requiresAuth: true } },
        { path: 'admin/tag/manage', name: 'tag-manage', component: () => import('@/views/admin/TagManage.vue'), meta: { requiresAuth: true } },
        { path: 'admin/tree/manage', name: 'tree-manage', component: () => import('@/views/admin/TreeManage.vue'), meta: { requiresAuth: true } },
        { path: 'admin/tree/node/:treeId', name: 'tree-node-manage', component: () => import('@/views/admin/TreeNodeManage.vue'), meta: { requiresAuth: true } }
      ]
    },
    { path: '/login', name: 'login', component: () => import('@/views/user/Login.vue') }
  ]
})

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth) {
    const userStore = useUserStore()
    if (!userStore.token) {
      next('/login')
      return
    }
  }
  next()
})

export default router
