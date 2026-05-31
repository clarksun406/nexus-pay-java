<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">Overview</h1>
    <div class="grid grid-cols-4 gap-6 mb-8">
      <div class="bg-white p-6 rounded-lg shadow">
        <p class="text-gray-500 text-sm">Total Payments</p>
        <p class="text-3xl font-bold">{{ stats.totalPayments }}</p>
      </div>
      <div class="bg-white p-6 rounded-lg shadow">
        <p class="text-gray-500 text-sm">Successful</p>
        <p class="text-3xl font-bold text-green-600">{{ stats.successful }}</p>
      </div>
      <div class="bg-white p-6 rounded-lg shadow">
        <p class="text-gray-500 text-sm">Failed</p>
        <p class="text-3xl font-bold text-red-600">{{ stats.failed }}</p>
      </div>
      <div class="bg-white p-6 rounded-lg shadow">
        <p class="text-gray-500 text-sm">Total Volume</p>
        <p class="text-3xl font-bold">${{ stats.volume }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
const stats = ref({ totalPayments: 0, successful: 0, failed: 0, volume: '0.00' })
</script>
