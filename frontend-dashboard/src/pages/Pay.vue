<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-100">
    <div class="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
      <h1 class="text-2xl font-bold mb-2">{{ link?.title }}</h1>
      <p class="text-gray-500 mb-6">{{ link?.description }}</p>
      <div class="text-3xl font-bold mb-6">${{ (link?.amount || 0) / 100 }} {{ link?.currency?.toUpperCase() }}</div>
      <form @submit.prevent="submitPayment">
        <div class="mb-4">
          <label class="block text-sm font-medium mb-1">Card Number</label>
          <input v-model="cardNumber" placeholder="4242 4242 4242 4242" class="w-full px-3 py-2 border rounded-lg" />
        </div>
        <button type="submit" class="w-full bg-blue-600 text-white py-3 rounded-lg">Pay Now</button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/lib/api'

const route = useRoute()
const link = ref<any>(null)
const cardNumber = ref('')

onMounted(async () => {
  link.value = (await api.get(`/pub/pay/${route.params.token}`)).data
})

async function submitPayment() {
  await api.post(`/pub/pay/${route.params.token}`, { paymentMethodType: 'card', paymentMethodId: 'pm_test' })
}
</script>
