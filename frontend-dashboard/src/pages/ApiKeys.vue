<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold">API Keys</h1>
      <button @click="createKey" class="bg-blue-600 text-white px-4 py-2 rounded-lg">Create Key</button>
    </div>
    <div class="bg-white rounded-lg shadow overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Name</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Type</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Mode</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Key</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="key in keys" :key="key.id">
            <td class="px-6 py-4 text-sm">{{ key.name }}</td>
            <td class="px-6 py-4 text-sm">{{ key.type }}</td>
            <td class="px-6 py-4 text-sm">{{ key.mode }}</td>
            <td class="px-6 py-4 text-sm font-mono">{{ key.prefix }}...</td>
            <td class="px-6 py-4">
              <button @click="revokeKey(key.id)" class="text-red-600 hover:underline">Revoke</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/lib/api'

const keys = ref<any[]>([])

onMounted(async () => { keys.value = (await api.get('/api-keys')).data })
async function createKey() { keys.value = (await api.post('/api-keys', { mode: 'TEST', type: 'SECRET', name: 'New Key' })).data }
async function revokeKey(id: string) { await api.delete(`/api-keys/${id}`); onMounted() }
</script>
