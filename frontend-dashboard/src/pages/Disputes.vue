<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">Disputes</h1>
    <div class="bg-white rounded-lg shadow overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Amount</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Reason</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Due By</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="d in disputes" :key="d.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 text-sm">{{ d.id.slice(0, 8) }}</td>
            <td class="px-6 py-4 text-sm">{{ (d.amount / 100).toFixed(2) }} {{ d.currency?.toUpperCase() }}</td>
            <td class="px-6 py-4 text-sm">{{ d.reason }}</td>
            <td class="px-6 py-4"><span :class="statusClass(d.status)">{{ d.status }}</span></td>
            <td class="px-6 py-4 text-sm">{{ d.dueBy ? new Date(d.dueBy).toLocaleDateString() : '-' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/lib/api'

const disputes = ref<any[]>([])

onMounted(async () => {
  const auth = JSON.parse(localStorage.getItem('auth') || '{}')
  const res = await api.get(`/merchants/${auth.merchantId}/disputes`)
  disputes.value = res.data
})

function statusClass(status: string) {
  return {
    'px-2 py-1 text-xs rounded-full': true,
    'bg-red-100 text-red-800': status === 'OPEN',
    'bg-yellow-100 text-yellow-800': status === 'UNDER_REVIEW',
    'bg-green-100 text-green-800': status === 'WON',
    'bg-gray-100 text-gray-800': status === 'LOST'
  }
}
</script>
