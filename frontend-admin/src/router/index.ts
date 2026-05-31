import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '../pages/AdminLayout.vue'

const routes = [
  {
    path: '/',
    component: AdminLayout,
    children: [
      { path: '', name: 'overview', component: () => import('../pages/AdminOverview.vue') },
      { path: 'organizations', name: 'organizations', component: () => import('../pages/Organizations.vue') },
      { path: 'merchants', name: 'merchants', component: () => import('../pages/Merchants.vue') },
      { path: 'monitoring', name: 'monitoring', component: () => import('../pages/Monitoring.vue') },
      { path: 'payment-methods', name: 'payment-methods', component: () => import('../pages/PaymentMethods.vue') },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
