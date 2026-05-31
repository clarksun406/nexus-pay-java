<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">客户管理</h1>

    <!-- 搜索和创建 -->
    <div class="flex justify-between items-center mb-4">
      <input v-model="search" type="text" placeholder="搜索客户..." 
             class="border rounded px-3 py-2 w-64" />
      <button @click="showCreate = true" class="bg-indigo-600 text-white px-4 py-2 rounded">
        添加客户
      </button>
    </div>

    <!-- 客户列表 -->
    <table class="w-full bg-white rounded shadow">
      <thead class="bg-gray-50">
        <tr>
          <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">ID</th>
          <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">邮箱</th>
          <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">姓名</th>
          <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">支付方式</th>
          <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">订阅</th>
          <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">创建时间</th>
          <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">操作</th>
        </tr>
      </thead>
      <tbody class="divide-y">
        <tr v-for="c in filteredCustomers" :key="c.id" class="hover:bg-gray-50">
          <td class="px-4 py-3 text-sm font-mono">{{ c.id.slice(0, 8) }}</td>
          <td class="px-4 py-3 text-sm">{{ c.email || '-' }}</td>
          <td class="px-4 py-3 text-sm">{{ c.name || '-' }}</td>
          <td class="px-4 py-3 text-sm">{{ c.paymentMethodCount || 0 }} 张卡</td>
          <td class="px-4 py-3 text-sm">{{ c.subscriptionCount || 0 }} 个</td>
          <td class="px-4 py-3 text-sm">{{ formatDate(c.createdAt) }}</td>
          <td class="px-4 py-3 text-sm">
            <button @click="viewCustomer(c)" class="text-indigo-600 hover:underline mr-3">查看</button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- 创建客户弹窗 -->
    <div v-if="showCreate" class="fixed inset-0 bg-black/50 flex items-center justify-center">
      <div class="bg-white rounded-lg p-6 w-96">
        <h2 class="text-lg font-bold mb-4">添加客户</h2>
        <div class="space-y-4">
          <input v-model="form.email" type="email" placeholder="邮箱" class="w-full border rounded px-3 py-2" />
          <input v-model="form.name" type="text" placeholder="姓名" class="w-full border rounded px-3 py-2" />
          <input v-model="form.phone" type="tel" placeholder="电话" class="w-full border rounded px-3 py-2" />
        </div>
        <div class="flex justify-end gap-3 mt-6">
          <button @click="showCreate = false" class="px-4 py-2 text-gray-600">取消</button>
          <button @click="createCustomer" class="px-4 py-2 bg-indigo-600 text-white rounded">创建</button>
        </div>
      </div>
    </div>

    <!-- 客户详情弹窗 -->
    <div v-if="selectedCustomer" class="fixed inset-0 bg-black/50 flex items-center justify-center">
      <div class="bg-white rounded-lg p-6 w-[600px] max-h-[80vh] overflow-y-auto">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-lg font-bold">客户详情</h2>
          <button @click="selectedCustomer = null" class="text-gray-400 hover:text-gray-600">✕</button>
        </div>
        
        <div class="grid grid-cols-2 gap-4 mb-6">
          <div><span class="text-gray-500">邮箱:</span> {{ selectedCustomer.email }}</div>
          <div><span class="text-gray-500">姓名:</span> {{ selectedCustomer.name }}</div>
          <div><span class="text-gray-500">电话:</span> {{ selectedCustomer.phone || '-' }}</div>
          <div><span class="text-gray-500">状态:</span> {{ selectedCustomer.status }}</div>
        </div>

        <!-- 支付方式 -->
        <h3 class="font-bold mb-2">支付方式</h3>
        <div v-if="paymentMethods.length" class="space-y-2 mb-6">
          <div v-for="pm in paymentMethods" :key="pm.id" 
               class="border rounded p-3 flex justify-between items-center">
            <div>
              <span class="font-medium">{{ pm.brand }}</span>
              <span class="text-gray-500 ml-2">•••• {{ pm.last4 }}</span>
              <span class="text-gray-400 ml-2">{{ pm.expiryMonth }}/{{ pm.expiryYear }}</span>
              <span v-if="pm.isDefault" class="ml-2 text-xs bg-green-100 text-green-700 px-2 py-0.5 rounded">默认</span>
            </div>
            <button @click="removePaymentMethod(pm.id)" class="text-red-600 text-sm">删除</button>
          </div>
        </div>
        <div v-else class="text-gray-400 text-sm mb-6">暂无支付方式</div>

        <!-- 订阅 -->
        <h3 class="font-bold mb-2">订阅</h3>
        <div v-if="subscriptions.length" class="space-y-2">
          <div v-for="s in subscriptions" :key="s.id" class="border rounded p-3">
            <div class="flex justify-between">
              <span class="font-medium">{{ s.name || s.planId }}</span>
              <span :class="statusClass(s.status)">{{ s.status }}</span>
            </div>
            <div class="text-sm text-gray-500 mt-1">
              {{ s.amount / 100 }} {{ s.currency.toUpperCase() }} / {{ s.interval }}
            </div>
          </div>
        </div>
        <div v-else class="text-gray-400 text-sm">暂无订阅</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../lib/api'

const search = ref('')
const customers = ref([])
const showCreate = ref(false)
const selectedCustomer = ref(null)
const paymentMethods = ref([])
const subscriptions = ref([])

const form = ref({ email: '', name: '', phone: '' })

const filteredCustomers = computed(() => {
  if (!search.value) return customers.value
  const q = search.value.toLowerCase()
  return customers.value.filter(c => 
    c.email?.toLowerCase().includes(q) || c.name?.toLowerCase().includes(q)
  )
})

onMounted(async () => {
  await loadCustomers()
})

async function loadCustomers() {
  const res = await api.get('/customers')
  customers.value = res.data
}

async function createCustomer() {
  await api.post('/customers', form.value)
  showCreate.value = false
  form.value = { email: '', name: '', phone: '' }
  await loadCustomers()
}

async function viewCustomer(c) {
  selectedCustomer.value = c
  const [pmRes, subRes] = await Promise.all([
    api.get(`/customers/${c.id}/payment-methods`),
    api.get(`/customers/${c.id}/subscriptions`)
  ])
  paymentMethods.value = pmRes.data
  subscriptions.value = subRes.data
}

async function removePaymentMethod(id) {
  if (!confirm('确定删除此支付方式？')) return
  await api.delete(`/customers/${selectedCustomer.value.id}/payment-methods/${id}`)
  await viewCustomer(selectedCustomer.value)
}

function formatDate(date) {
  return new Date(date).toLocaleDateString('zh-CN')
}

function statusClass(status) {
  const classes = {
    ACTIVE: 'text-green-600',
    TRIALING: 'text-blue-600',
    PAST_DUE: 'text-red-600',
    CANCELED: 'text-gray-400'
  }
  return classes[status] || 'text-gray-600'
}
</script>
