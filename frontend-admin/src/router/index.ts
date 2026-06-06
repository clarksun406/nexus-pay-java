import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import AdminLayout from '../pages/AdminLayout.vue'

const routes = [
  { path: '/', redirect: '/admin' },
  {
    path: '/login',
    name: 'login',
    component: () => import('../pages/AdminLogin.vue'),
    meta: { guest: true }
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'overview', component: () => import('../pages/AdminOverview.vue') },
      { path: 'organizations', name: 'organizations', component: () => import('../pages/Organizations.vue') },
      { path: 'merchants', name: 'merchants', component: () => import('../pages/MerchantManagement.vue') },
      { path: 'providers', name: 'providers', component: () => import('../pages/ProviderConfig.vue') },
      { path: 'reports', name: 'reports', component: () => import('../pages/Reports.vue') },
      { path: 'audit-logs', name: 'audit-logs', component: () => import('../pages/AuditLogs.vue') },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Route guard
router.beforeEach((to, _from, next) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    next({ name: 'login' })
  } else if (to.meta.guest && auth.isAuthenticated) {
    next({ name: 'overview' })
  } else {
    next()
  }
})

export default router