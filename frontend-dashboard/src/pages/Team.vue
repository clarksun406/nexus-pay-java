<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">Team</h1>
    <div class="bg-white rounded-lg shadow">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">User</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Role</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="m in members" :key="m.id">
            <td class="px-6 py-4">{{ m.userId }}</td>
            <td class="px-6 py-4">{{ m.role }}</td>
            <td class="px-6 py-4">{{ m.status }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/lib/api'
const members = ref<any[]>([])
onMounted(async () => { members.value = (await api.get('/members')).data })
</script>
