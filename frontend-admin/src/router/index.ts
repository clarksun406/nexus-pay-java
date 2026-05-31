import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '../pages/AdminLayout.vue'

const routes = [
  { path: '/', redirect: '/admin' },
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      { path: '', name: 'overview', component: () => import('../pages/AdminOverview.vue') },
      { path: 'organizations', name: 'organizations', component: () => import('../pages/Organizations.vue') },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
