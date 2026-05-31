<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">订阅管理</h1>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-4 gap-4 mb-6">
      <div class="bg-white rounded shadow p-4">
        <div class="text-2xl font-bold">{{ stats.active }}</div>
        <div class="text-gray-500 text-sm">活跃订阅</div>
      </div>
      <div class="bg-white rounded shadow p-4">
        <div class="text-2xl font-bold text-blue-600">{{ stats.trialing }}</div>
        <div class="text-gray-500 text-sm">试用中</div>
      </div>
      <div class="bg-white rounded shadow p-4">
        <div class="text-2xl font-bold text-red-600">{{ stats.pastDue }}</div>
        <div class="text-gray-500 text-sm">逾期</div>
      </div>
      <div class="bg-white rounded shadow p-4">
        <div class="text-2xl font-bold text-gray-400">{{ stats.canceled }}</div>
        <div class="text-gray-500 text-sm">已取消</div>
      </div>
    </div>

    <!-- 筛选 -->
    <div class="flex gap-4 mb-4">
      <select v-model="filter.status" class="border rounded px-3 py-2">
        <option value="">全部状态</option>
        <option value="ACTIVE">活跃</option>
        <option value="TRIALING">试用中</option>
        <option value="PAST_DUE">逾期</option>
        <option value="CANCELED">已取消</option>
      </select>
      <button @click="showCreate = true" class="bg-indigo-600 text-white px-4 py-2 rounded">
        创建订阅
      </button>
    </div>

    <!-- 订阅列表 -->
    <table class="w-full bg-white rounded shadow">
      <thead class="bg-gray-50">
        <tr>
          <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">ID</th>
          <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">客户</th>
          <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">计划</th>
          <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">金额</th>
          <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">状态</th>
          <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">下次扣款</th>
          <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">操作</th>
        </tr>
      </thead>
      <tbody class="divide-y">
        <tr v-for="s in filteredSubscriptions" :key="s.id" class="hover:bg-gray-50">
          <td class="px-4 py-3 text-sm font-mono">{{ s.id.slice(0, 8) }}</td>
          <td class="px-4 py-3 text-sm">{{ s.customerName || s.customerId.slice(0, 8) }}</td>
          <td class="px-4 py-3 text-sm">{{ s.name || s.planId || '-' }}</td>
          <td class="px-4 py-3 text-sm">{{ formatAmount(s) }}</td>
          <td class="px-4 py-3">
            <span :class="statusBadge(s.status)" class="px-2 py-1 rounded text-xs font-medium">
              {{ statusText(s.status) }}
            </span>
          </td>
          <td class="px-4 py-3 text-sm">{{ formatDate(s.currentPeriodEnd) }}</td>
          <td class="px-4 py-3 text-sm">
            <button v-if="s.status === 'INCOMPLETE'" @click="activate(s.id)" 
                    class="text-green-600 hover:underline mr-2">激活</button>
            <button v-if="s.status === 'ACTIVE' || s.status === 'TRIALING'" 
                    @click="cancelSubscription(s.id)" class="text-red-600 hover:underline">取消</button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- 创建订阅弹窗 -->
    <div v-if="showCreate" class="fixed inset-0 bg-black/50 flex items-center justify-center">
      <div class="bg-white rounded-lg p-6 w-96">
        <h2 class="text-lg font-bold mb-4">创建订阅</h2>
        <div class="space-y-4">
          <select v-model="form.customerId" class="w-full border rounded px-3 py-2">
            <option value="">选择客户</option>
            <option v-for="c in customers" :key="c.id" :value="c.id">{{ c.email || c.name || c.id }}</option>
          </select>
          <input v-model="form.name" type="text" placeholder="订阅名称" class="w-full border rounded px-3 py-2" />
          <div class="grid grid-cols-2 gap-2">
            <input v-model.number="form.amount" type="number" placeholder="金额(分)" class="border rounded px-3 py-2" />
            <select v-model="form.currency" class="border rounded px-3 py-2">
              <option value="cny">CNY</option>
              <option value="usd">USD</option>
            </select>
          </div>
          <div class="grid grid-cols-2 gap-2">
            <select v-model="form.interval" class="border rounded px-3 py-2">
              <option value="MONTH">每月</option>
              <option value="YEAR">每年</option>
            </select>
            <input v-model.number="form.trialDays" type="number" placeholder="试用期(天)" class="border rounded px-3 py-2" />
          </div>
        </div>
        <div class="flex justify-end gap-3 mt-6">
          <button @click="showCreate = false" class="px-4 py-2 text-gray-600">取消</button>
          <button @click="createSubscription" class="px-4 py-2 bg-indigo-600 text-white rounded">创建</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../lib/api'

const filter = ref({ status: '' })
const subscriptions = ref([])
const customers = ref([])
const showCreate = ref(false)

const form = ref({
  customerId: '',
  name: '',
  amount: 100,
  currency: 'cny',
  interval: 'MONTH',
  trialDays: 0
})

const stats = computed(() => ({
  active: subscriptions.value.filter(s => s.status === 'ACTIVE').length,
  trialing: subscriptions.value.filter(s => s.status === 'TRIALING').length,
  pastDue: subscriptions.value.filter(s => s.status === 'PAST_DUE').length,
  canceled: subscriptions.value.filter(s => s.status === 'CANCELED').length
}))

const filteredSubscriptions = computed(() => {
  if (!filter.value.status) return subscriptions.value
  return subscriptions.value.filter(s => s.status === filter.value.status)
})

onMounted(async () => {
  await Promise.all([loadSubscriptions(), loadCustomers()])
})

async function loadSubscriptions() {
  const res = await api.get('/subscriptions')
  subscriptions.value = res.data
}

async function loadCustomers() {
  const res = await api.get('/customers')
  customers.value = res.data
}

async function createSubscription() {
  await api.post('/subscriptions', form.value)
  showCreate.value = false
  await loadSubscriptions()
}

async function activate(id) {
  await api.post(`/subscriptions/${id}/activate`)
  await loadSubscriptions()
}

async function cancelSubscription(id) {
  if (!confirm('确定取消此订阅？')) return
  await api.post(`/subscriptions/${id}/cancel?immediately=true`)
  await loadSubscriptions()
}

function formatAmount(s) {
  return `${(s.amount / 100).toFixed(2)} ${s.currency.toUpperCase()} / ${s.intervalCount} ${s.interval.toLowerCase()}`
}

function formatDate(date) {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

function statusBadge(status) {
  const badges = {
    ACTIVE: 'bg-green-100 text-green-700',
    TRIALING: 'bg-blue-100 text-blue-700',
    PAST_DUE: 'bg-red-100 text-red-700',
    CANCELED: 'bg-gray-100 text-gray-500',
    INCOMPLETE: 'bg-yellow-100 text-yellow-700'
  }
  return badges[status] || 'bg-gray-100 text-gray-600'
}

function statusText(status) {
  const texts = {
    ACTIVE: '活跃',
    TRIALING: '试用中',
    PAST_DUE: '逾期',
    CANCELED: '已取消',
    INCOMPLETE: '待激活'
  }
  return texts[status] || status
}
</script>
