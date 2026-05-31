<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">Routing Rules</h1>
    <div class="bg-white rounded-lg shadow">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Priority</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Target</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="r in rules" :key="r.id">
            <td class="px-6 py-4">{{ r.priority }}</td>
            <td class="px-6 py-4">{{ r.targetProvider }}</td>
            <td class="px-6 py-4"><span :class="r.enabled ? 'bg-green-100 text-green-800' : 'bg-gray-100'" class="px-2 py-1 text-xs rounded">{{ r.enabled ? 'Active' : 'Disabled' }}</span></td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/lib/api'
const rules = ref<any[]>([])
onMounted(async () => { rules.value = (await api.get('/routing-rules')).data })
</script>
