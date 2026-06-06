<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '../lib/api'

const merchants = ref<any[]>([])
const loading = ref(true)

onMounted(async () => {
  try { const res = await api.get("/merchants"); merchants.value = res.data }
  finally { loading.value = false }
})

async function approve(id: string) {
  await api.post("/merchants/" + id + "/approve")
  merchants.value = merchants.value.map(m => m.id === id ? { ...m, status: "ACTIVE" } : m)
}

async function rejectM(id: string) {
  await api.post("/merchants/" + id + "/reject", { reason: "Rejected by admin" })
  merchants.value = merchants.value.map(m => m.id === id ? { ...m, status: "SUSPENDED" } : m)
}
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">Merchant Management</h1>
    <div v-if="loading" class="text-gray-500">Loading...</div>
    <div v-else class="bg-white rounded-lg shadow overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Name</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Created</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="m in merchants" :key="m.id" class="hover:bg-gray-50">
            <td class="px-4 py-3 text-sm font-medium">{{ m.name }}</td>
            <td class="px-4 py-3"><span :class="m.status === "ACTIVE" ? "text-green-700 bg-green-100" : "text-red-700 bg-red-100"" class="px-2 py-1 rounded-full text-xs font-medium">{{ m.status }}</span></td>
            <td class="px-4 py-3 text-sm text-gray-500">{{ m.createdAt?.substring(0, 10) }}</td>
            <td class="px-4 py-3 space-x-2">
              <button v-if="m.status !== "ACTIVE"" @click="approve(m.id)" class="text-green-600 hover:text-green-800 text-sm">Approve</button>
              <button v-if="m.status === "ACTIVE"" @click="rejectM(m.id)" class="text-red-600 hover:text-red-800 text-sm">Suspend</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>