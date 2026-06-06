<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '../lib/api'

const logs = ref<any[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await api.get("/monitoring")
    logs.value = res.data.recentErrors || []
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">Audit Logs</h1>
    <div v-if="loading" class="text-gray-500">Loading...</div>
    <div v-else-if="logs.length === 0" class="bg-white rounded-lg shadow p-6 text-gray-500">No audit entries found.</div>
    <div v-else class="bg-white rounded-lg shadow overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Event</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Provider</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Timestamp</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Details</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="l in logs" :key="l.code" class="hover:bg-gray-50">
            <td class="px-4 py-3 text-sm font-medium">{{ l.message }}</td>
            <td class="px-4 py-3 text-sm">{{ l.provider || "-" }}</td>
            <td class="px-4 py-3 text-sm text-gray-500">{{ l.timestamp?.substring(0, 19) }}</td>
            <td class="px-4 py-3 text-sm text-gray-500 font-mono">{{ l.code }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>