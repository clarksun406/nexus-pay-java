<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">Payouts</h1>
    <div class="bg-white rounded-lg shadow overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Period</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Amount</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Fee</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Net</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="p in payouts" :key="p.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 text-sm">
              {{ new Date(p.periodStart).toLocaleDateString() }} - {{ new Date(p.periodEnd).toLocaleDateString() }}
            </td>
            <td class="px-6 py-4 text-sm">{{ (p.amount / 100).toFixed(2) }} {{ p.currency?.toUpperCase() }}</td>
            <td class="px-6 py-4 text-sm text-red-600">-{{ (p.feeAmount / 100).toFixed(2) }}</td>
            <td class="px-6 py-4 text-sm font-medium text-green-600">{{ (p.netAmount / 100).toFixed(2) }}</td>
            <td class="px-6 py-4"><span :class="statusClass(p.status)">{{ p.status }}</span></td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/lib/api'

const payouts = ref<any[]>([])

onMounted(async () => {
  const auth = JSON.parse(localStorage.getItem('auth') || '{}')
  const res = await api.get(`/merchants/${auth.merchantId}/payouts`)
  payouts.value = res.data
})

function statusClass(status: string) {
  return {
    'px-2 py-1 text-xs rounded-full': true,
    'bg-yellow-100 text-yellow-800': status === 'PENDING',
    'bg-blue-100 text-blue-800': status === 'PROCESSING',
    'bg-green-100 text-green-800': status === 'PAID',
    'bg-red-100 text-red-800': status === 'FAILED'
  }
}
</script>
