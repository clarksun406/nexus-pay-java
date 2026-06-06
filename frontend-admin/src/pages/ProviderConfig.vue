<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '../lib/api'

const methods = ref<any[]>([])

onMounted(async () => {
  try { const res = await api.get("/payment-methods"); methods.value = res.data }
  catch { methods.value = [] }
})

async function toggle(method: string, enabled: boolean) {
  await api.put("/payment-methods/" + method, { enabled: !enabled })
  methods.value = methods.value.map(m => m.method === method ? { ...m, enabled: !enabled } : m)
}
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">Provider Configuration</h1>
    <div class="bg-white rounded-lg shadow overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Payment Method</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Regions</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="pm in methods" :key="pm.method" class="hover:bg-gray-50">
            <td class="px-4 py-3 text-sm font-medium capitalize">{{ pm.method }}</td>
            <td class="px-4 py-3"><span :class="pm.enabled ? "text-green-700 bg-green-100" : "text-gray-700 bg-gray-100"" class="px-2 py-1 rounded-full text-xs font-medium">{{ pm.enabled ? "Enabled" : "Disabled" }}</span></td>
            <td class="px-4 py-3 text-sm text-gray-500">{{ pm.supportedRegions?.join(", ") || "All" }}</td>
            <td class="px-4 py-3">
              <button @click="toggle(pm.method, pm.enabled)" class="text-blue-600 hover:text-blue-800 text-sm">
                {{ pm.enabled ? "Disable" : "Enable" }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>