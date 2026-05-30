import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/lib/api'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(localStorage.getItem('accessToken'))
  const user = ref<any>(null)
  const merchant = ref<any>(null)

  const isAuthenticated = computed(() => !!accessToken.value)

  async function login(email: string, password: string) {
    const res = await api.post('/auth/login', { email, password })
    setTokens(res.data.accessToken, res.data.refreshToken)
    user.value = { id: res.data.userId, email: res.data.email }
    merchant.value = { id: res.data.merchantId, name: res.data.merchantName }
  }

  async function register(data: { email: string; password: string; organizationName: string; merchantName: string }) {
    const res = await api.post('/auth/register', data)
    setTokens(res.data.accessToken, res.data.refreshToken)
    user.value = { id: res.data.userId, email: res.data.email }
    merchant.value = { id: res.data.merchantId, name: res.data.merchantName }
  }

  async function fetchMe() {
    const res = await api.get('/me')
    user.value = res.data
  }

  function logout() {
    accessToken.value = null
    user.value = null
    merchant.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }

  function setTokens(at: string, rt: string) {
    accessToken.value = at
    localStorage.setItem('accessToken', at)
    localStorage.setItem('refreshToken', rt)
  }

  return { accessToken, user, merchant, isAuthenticated, login, register, fetchMe, logout }
})
