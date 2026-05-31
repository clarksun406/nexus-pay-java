<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">运营概览</h1>

    <!-- Stats Grid -->
    <div class="grid grid-cols-5 gap-4 mb-8">
      <div class="bg-white rounded-lg shadow p-6">
        <div class="text-3xl font-bold text-indigo-600">{{ stats.totalOrganizations }}</div>
        <div class="text-gray-500 text-sm mt-1">组织总数</div>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <div class="text-3xl font-bold text-green-600">{{ stats.totalMerchants }}</div>
        <div class="text-gray-500 text-sm mt-1">商户总数</div>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <div class="text-3xl font-bold text-blue-600">{{ formatNumber(stats.totalPayments) }}</div>
        <div class="text-gray-500 text-sm mt-1">支付笔数</div>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <div class="text-3xl font-bold text-purple-600">¥{{ formatVolume(stats.totalVolume) }}</div>
        <div class="text-gray-500 text-sm mt-1">交易金额</div>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <div class="text-3xl font-bold text-amber-600">{{ stats.pendingApprovals }}</div>
        <div class="text-gray-500 text-sm mt-1">待审核</div>
      </div>
    </div>

    <!-- Two columns -->
    <div class="grid grid-cols-2 gap-6">
      <!-- Pending Approvals -->
      <div class="bg-white rounded-lg shadow">
        <div class="p-4 border-b flex justify-between items-center">
          <h2 class="font-bold">待审核商户</h2>
          <span class="text-sm text-gray-500">{{ pending.length }} 条</span>
        </div>
        <div class="p-4">
          <div v-for="p in pending" :key="p.id" class="flex items-center justify-between py-3 border-b last:border-0">
            <div>
              <div class="font-medium">{{ p.name }}</div>
              <div class="text-sm text-gray-500">{{ p.email }}</div>
            </div>
            <div class="flex gap-2">
              <button @click="approve(p.id)" class="px-3 py-1 bg-green-100 text-green-700 rounded text-sm hover:bg-green-200">
                通过
              </button>
              <button @click="reject(p.id)" class="px-3 py-1 bg-red-100 text-red-700 rounded text-sm hover:bg-red-200">
                拒绝
              </button>
            </div>
          </div>
          <div v-if="!pending.length" class="text-center text-gray-400 py-6">暂无待审核</div>
        </div>
      </div>

      <!-- Provider Health -->
      <div class="bg-white rounded-lg shadow">
        <div class="p-4 border-b">
          <h2 class="font-bold">Provider 状态</h2>
        </div>
        <div class="p-4">
          <div v-for="p in providers" :key="p.provider" class="flex items-center justify-between py-3 border-b last:border-0">
            <div class="flex items-center gap-3">
              <span :class="statusDot(p.status)" class="w-3 h-3 rounded-full"></span>
              <span class="font-medium">{{ p.provider }}</span>
            </div>
            <div class="text-right">
              <div class="text-sm text-gray-500">{{ p.uptime }}% 正常</div>
              <div class="text-xs text-gray-400">{{ p.requestsPerMin }} req/min</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../lib/api'

const stats = ref({
  totalOrganizations: 0,
  totalMerchants: 0,
  totalPayments: 0,
  totalVolume: 0,
  successRate: 0,
  pendingApprovals: 0
})

const pending = ref([])
const providers = ref([])

onMounted(async () => {
  const [overview, pendingRes, monitoring] = await Promise.all([
    api.get('/admin/overview'),
    api.get('/admin/pending-approvals'),
    api.get('/admin/monitoring')
  ])
  stats.value = overview.data
  pending.value = pendingRes.data
  providers.value = monitoring.data.providers
})

async function approve(id) {
  await api.post(`/admin/merchants/${id}/approve`)
  pending.value = pending.value.filter(p => p.id !== id)
}

async function reject(id) {
  const reason = prompt('拒绝原因:')
  if (!reason) return
  await api.post(`/admin/merchants/${id}/reject`, { reason })
  pending.value = pending.value.filter(p => p.id !== id)
}

function statusDot(status) {
  return status === 'healthy' ? 'bg-green-500' : status === 'degraded' ? 'bg-yellow-500' : 'bg-red-500'
}

function formatNumber(n) {
  return n >= 1000 ? `${(n / 1000).toFixed(1)}k` : n
}

function formatVolume(v) {
  return v >= 1000000 ? `${(v / 1000000).toFixed(1)}M` : v >= 1000 ? `${(v / 1000).toFixed(1)}k` : v
}
</script>
