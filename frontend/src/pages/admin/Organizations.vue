<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">组织管理</h1>

    <div class="flex justify-between items-center mb-4">
      <input v-model="search" type="text" placeholder="搜索组织..." class="border rounded px-3 py-2 w-64" />
      <button @click="showCreate = true" class="bg-indigo-600 text-white px-4 py-2 rounded">创建组织</button>
    </div>

    <table class="w-full bg-white rounded shadow">
      <thead class="bg-gray-50">
        <tr>
          <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">ID</th>
          <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">名称</th>
          <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">商户数</th>
          <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">状态</th>
          <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">创建时间</th>
          <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">操作</th>
        </tr>
      </thead>
      <tbody class="divide-y">
        <tr v-for="org in filteredOrgs" :key="org.id" class="hover:bg-gray-50">
          <td class="px-4 py-3 text-sm font-mono">{{ org.id.slice(0, 8) }}</td>
          <td class="px-4 py-3 text-sm font-medium">{{ org.name }}</td>
          <td class="px-4 py-3 text-sm">{{ orgStats[org.id]?.totalMerchants || 0 }}</td>
          <td class="px-4 py-3">
            <span :class="org.status === 'ACTIVE' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'"
                  class="px-2 py-1 rounded text-xs font-medium">
              {{ org.status }}
            </span>
          </td>
          <td class="px-4 py-3 text-sm text-gray-500">{{ formatDate(org.createdAt) }}</td>
          <td class="px-4 py-3 text-sm">
            <button @click="viewOrg(org)" class="text-indigo-600 hover:underline mr-3">详情</button>
            <button v-if="org.status === 'ACTIVE'" @click="suspendOrg(org.id)" class="text-red-600 hover:underline">停用</button>
            <button v-else @click="activateOrg(org.id)" class="text-green-600 hover:underline">启用</button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- Create Modal -->
    <div v-if="showCreate" class="fixed inset-0 bg-black/50 flex items-center justify-center">
      <div class="bg-white rounded-lg p-6 w-96">
        <h2 class="text-lg font-bold mb-4">创建组织</h2>
        <input v-model="form.name" type="text" placeholder="组织名称" class="w-full border rounded px-3 py-2 mb-4" />
        <div class="flex justify-end gap-3">
          <button @click="showCreate = false" class="px-4 py-2 text-gray-600">取消</button>
          <button @click="createOrg" class="px-4 py-2 bg-indigo-600 text-white rounded">创建</button>
        </div>
      </div>
    </div>

    <!-- Detail Modal -->
    <div v-if="selectedOrg" class="fixed inset-0 bg-black/50 flex items-center justify-center">
      <div class="bg-white rounded-lg p-6 w-[700px] max-h-[80vh] overflow-y-auto">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-lg font-bold">{{ selectedOrg.name }}</h2>
          <button @click="selectedOrg = null" class="text-gray-400 hover:text-gray-600">✕</button>
        </div>

        <h3 class="font-bold mb-2">商户列表</h3>
        <div class="space-y-2 mb-4">
          <div v-for="m in merchants" :key="m.id" class="border rounded p-3 flex justify-between items-center">
            <div>
              <span class="font-medium">{{ m.name }}</span>
              <span :class="m.status === 'ACTIVE' ? 'text-green-600' : 'text-red-600'" class="ml-2 text-sm">
                {{ m.status }}
              </span>
            </div>
            <button v-if="m.status === 'ACTIVE'" @click="suspendMerchant(m.id)" class="text-red-600 text-sm">停用</button>
            <button v-else @click="activateMerchant(m.id)" class="text-green-600 text-sm">启用</button>
          </div>
          <div v-if="!merchants.length" class="text-gray-400 text-sm">暂无商户</div>
        </div>

        <button @click="showAddMerchant = true" class="text-indigo-600 text-sm">+ 添加商户</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../../lib/api'

const search = ref('')
const organizations = ref([])
const orgStats = ref({})
const showCreate = ref(false)
const selectedOrg = ref(null)
const merchants = ref([])

const form = ref({ name: '' })

const filteredOrgs = computed(() => {
  if (!search.value) return organizations.value
  return organizations.value.filter(o => o.name.toLowerCase().includes(search.value.toLowerCase()))
})

onMounted(async () => {
  const res = await api.get('/admin/organizations')
  organizations.value = res.data
  // Load stats for each org
  for (const org of organizations.value) {
    try {
      const statsRes = await api.get(`/admin/organizations/${org.id}/stats`)
      orgStats.value[org.id] = statsRes.data
    } catch (e) {}
  }
})

async function createOrg() {
  await api.post('/admin/organizations', form.value)
  showCreate.value = false
  form.value = { name: '' }
  const res = await api.get('/admin/organizations')
  organizations.value = res.data
}

async function suspendOrg(id) {
  await api.delete(`/admin/organizations/${id}`)
  organizations.value = organizations.value.map(o => o.id === id ? { ...o, status: 'INACTIVE' } : o)
}

async function activateOrg(id) {
  await api.put(`/admin/organizations/${id}`, { status: 'ACTIVE' })
  organizations.value = organizations.value.map(o => o.id === id ? { ...o, status: 'ACTIVE' } : o)
}

async function viewOrg(org) {
  selectedOrg.value = org
  const res = await api.get(`/admin/organizations/${org.id}/merchants`)
  merchants.value = res.data
}

async function suspendMerchant(id) {
  await api.post(`/admin/organizations/${selectedOrg.value.id}/merchants/${id}/suspend`)
  const res = await api.get(`/admin/organizations/${selectedOrg.value.id}/merchants`)
  merchants.value = res.data
}

async function activateMerchant(id) {
  await api.post(`/admin/organizations/${selectedOrg.value.id}/merchants/${id}/activate`)
  const res = await api.get(`/admin/organizations/${selectedOrg.value.id}/merchants`)
  merchants.value = res.data
}

function formatDate(date) {
  return new Date(date).toLocaleDateString('zh-CN')
}
</script>
