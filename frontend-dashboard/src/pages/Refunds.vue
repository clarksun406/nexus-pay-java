<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '../lib/api'

const refunds = ref<any[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await api.get("/refunds")
    refunds.value = res.data
  } finally {
    loading.value = false
  }
})

function statusClass(status: string) {
  return status === "SUCCEEDED" ? "text-green-700 bg-green-100"
    : status === "FAILED" ? "text-red-700 bg-red-100"
    : "text-yellow-700 bg-yellow-100"
}
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">Refunds</h1>
    <div v-if="loading" class="text-gray-500">Loading...</div>
    <div v-else-if="refunds.length === 0" class="bg-white rounded-lg shadow p-6 text-gray-500">
      No refunds yet.
    </div>
    <div v-else class="bg-white rounded-lg shadow overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Payment</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Amount</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Reason</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Created</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="r in refunds" :key="r.id" class="hover:bg-gray-50">
            <td class="px-4 py-3 text-sm font-mono">{{ r.id?.substring(0, 8) }}...</td>
            <td class="px-4 py-3 text-sm font-mono">{{ r.paymentIntentId?.substring(0, 8) }}...</td>
            <td class="px-4 py-3 text-sm">{{ r.amount }} {{ r.currency?.toUpperCase() }}</td>
            <td class="px-4 py-3">
              <span :class="statusClass(r.status)" class="px-2 py-1 rounded-full text-xs font-medium">
                {{ r.status }}
              </span>
            </td>
            <td class="px-4 py-3 text-sm">{{ r.reason || "-" }}</td>
            <td class="px-4 py-3 text-sm text-gray-500">{{ r.createdAt?.substring(0, 10) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>