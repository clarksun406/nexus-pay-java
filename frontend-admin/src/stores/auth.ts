import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../lib/api'

interface User {
  userId: string
  email: string
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const accessToken = ref<string | null>(localStorage.getItem('admin_token'))
  const refreshToken = ref<string | null>(localStorage.getItem('admin_refresh'))
  const permissions = ref<string[]>([])

  const isAuthenticated = computed(() => !!accessToken.value)
  const isAdmin = computed(() => isAuthenticated.value)

  function hasPermission(code: string): boolean {
    return permissions.value.includes(code)
  }

  function hasAnyPermission(...codes: string[]): boolean {
    return codes.some(c => permissions.value.includes(c))
  }

  function hasAllPermissions(...codes: string[]): boolean {
    return codes.every(c => permissions.value.includes(c))
  }

  async function login(email: string, password: string) {
    const res = await api.post('/auth/login', { email, password })
    const data = res.data
    accessToken.value = data.accessToken
    refreshToken.value = data.refreshToken
    user.value = { userId: data.userId, email: data.email }
    localStorage.setItem('admin_token', data.accessToken)
    localStorage.setItem('admin_refresh', data.refreshToken)
    await loadPermissions()
  }

  async function loadPermissions() {
    try {
      const res = await api.get('/me/permissions')
      permissions.value = res.data ?? []
    } catch {
      permissions.value = []
    }
  }

  async function refresh() {
    if (!refreshToken.value) throw new Error('No refresh token')
    const res = await api.post('/auth/refresh', { refreshToken: refreshToken.value })
    const data = res.data
    accessToken.value = data.accessToken
    refreshToken.value = data.refreshToken
    localStorage.setItem('admin_token', data.accessToken)
    localStorage.setItem('admin_refresh', data.refreshToken)
  }

  function logout() {
    accessToken.value = null
    refreshToken.value = null
    user.value = null
    permissions.value = []
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_refresh')
  }

  return {
    user, accessToken, refreshToken, permissions,
    isAuthenticated, isAdmin,
    hasPermission, hasAnyPermission, hasAllPermissions,
    login, logout, refresh, loadPermissions
  }
})