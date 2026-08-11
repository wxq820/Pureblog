import { createRouter, createWebHistory } from 'vue-router'

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
        { path: 'notifications', name: 'notifications', component: () => import('@/views/notification/NotificationList.vue'), meta: { requiresAuth: true } }
      ]
    },
    { path: '/login', name: 'login', component: () => import('@/views/user/Login.vue') }
  ]
})

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token')
    if (!token) {
      next('/login')
      return
    }
  }
  next()
})

export default router
