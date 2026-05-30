<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">Payment Details</h1>
    <div class="bg-white rounded-lg shadow p-6">
      <dl class="grid grid-cols-2 gap-4">
        <dt class="text-gray-500">ID</dt><dd>{{ payment?.id }}</dd>
        <dt class="text-gray-500">Amount</dt><dd>{{ payment?.amount }} {{ payment?.currency }}</dd>
        <dt class="text-gray-500">Status</dt><dd>{{ payment?.status }}</dd>
        <dt class="text-gray-500">Provider</dt><dd>{{ payment?.resolvedProvider || '-' }}</dd>
      </dl>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/lib/api'

const route = useRoute()
const payment = ref<any>(null)

onMounted(async () => {
  payment.value = (await api.get(`/payment-intents/${route.params.id}`)).data
})
</script>
