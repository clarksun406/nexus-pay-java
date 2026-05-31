<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">Payments</h1>
    <div class="bg-white rounded-lg shadow overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Amount</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Provider</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Created</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="p in payments" :key="p.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 text-sm">{{ p.id.slice(0, 8) }}</td>
            <td class="px-6 py-4 text-sm">{{ (p.amount / 100).toFixed(2) }} {{ p.currency.toUpperCase() }}</td>
            <td class="px-6 py-4"><span :class="statusClass(p.status)">{{ p.status }}</span></td>
            <td class="px-6 py-4 text-sm">{{ p.resolvedProvider || '-' }}</td>
            <td class="px-6 py-4 text-sm">{{ new Date(p.createdAt).toLocaleDateString() }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/lib/api'

const payments = ref<any[]>([])

onMounted(async () => {
  const res = await api.get('/payment-intents')
  payments.value = res.data
})

function statusClass(status: string) {
  return {
    'px-2 py-1 text-xs rounded-full': true,
    'bg-green-100 text-green-800': status === 'SUCCEEDED',
    'bg-red-100 text-red-800': status === 'FAILED',
    'bg-yellow-100 text-yellow-800': status === 'PROCESSING',
    'bg-gray-100 text-gray-800': true
  }
}
</script>
