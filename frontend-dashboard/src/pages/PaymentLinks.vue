<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">Payment Links</h1>
    <div class="bg-white rounded-lg shadow">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Title</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Amount</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Link</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="l in links" :key="l.id">
            <td class="px-6 py-4">{{ l.title }}</td>
            <td class="px-6 py-4">${{ l.amount / 100 }}</td>
            <td class="px-6 py-4">{{ l.status }}</td>
            <td class="px-6 py-4 text-blue-600 cursor-pointer" @click="copyLink(l.token)">Copy Link</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/lib/api'

const links = ref<any[]>([])
onMounted(async () => { links.value = (await api.get('/payment-links')).data })

function copyLink(token: string) {
  navigator.clipboard.writeText(`${window.location.origin}/pub/pay/${token}`)
}
</script>
