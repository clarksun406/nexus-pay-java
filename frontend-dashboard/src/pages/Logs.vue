<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">API Logs</h1>
    <div class="bg-white rounded-lg shadow overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Time</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Method</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Path</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Duration</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="log in logs" :key="log.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 text-sm">{{ new Date(log.createdAt).toLocaleString() }}</td>
            <td class="px-6 py-4"><span :class="methodClass(log.method)">{{ log.method }}</span></td>
            <td class="px-6 py-4 text-sm font-mono">{{ log.path }}</td>
            <td class="px-6 py-4"><span :class="statusClass(log.statusCode)">{{ log.statusCode }}</span></td>
            <td class="px-6 py-4 text-sm">{{ log.durationMs }}ms</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/lib/api'

const logs = ref<any[]>([])

onMounted(async () => {
  // In production, fetch from /api/v1/merchants/:id/logs
  logs.value = []
})

function methodClass(method: string) {
  return {
    'px-2 py-1 text-xs rounded font-mono': true,
    'bg-green-100 text-green-800': method === 'GET',
    'bg-blue-100 text-blue-800': method === 'POST',
    'bg-yellow-100 text-yellow-800': method === 'PUT',
    'bg-red-100 text-red-800': method === 'DELETE'
  }
}

function statusClass(status: number) {
  return {
    'px-2 py-1 text-xs rounded': true,
    'bg-green-100 text-green-800': status < 300,
    'bg-yellow-100 text-yellow-800': status >= 300 && status < 400,
    'bg-red-100 text-red-800': status >= 400
  }
}
</script>
