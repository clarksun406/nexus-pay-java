<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">Connectors</h1>
    <div class="grid gap-4">
      <div v-for="c in connectors" :key="c.id" class="bg-white p-6 rounded-lg shadow flex justify-between items-center">
        <div>
          <h3 class="font-bold">{{ c.label }}</h3>
          <p class="text-gray-500 text-sm">{{ c.provider }} • {{ c.mode }}</p>
        </div>
        <span :class="c.isPrimary ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'" class="px-2 py-1 text-xs rounded">
          {{ c.isPrimary ? 'Primary' : 'Weight: ' + c.weight }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/lib/api'
const connectors = ref<any[]>([])
onMounted(async () => { connectors.value = (await api.get('/connectors')).data })
</script>
