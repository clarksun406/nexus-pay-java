<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '../lib/api'

const endpoints = ref<any[]>([])
const loading = ref(true)
const showForm = ref(false)
const newUrl = ref('')
const newEvents = ref('payment_intent.succeeded,payment_intent.failed')

onMounted(async () => {
  try {
    const res = await api.get("/webhook-endpoints")
    endpoints.value = res.data
  } finally {
    loading.value = false
  }
})

async function createEndpoint() {
  await api.post("/webhook-endpoints", {
    url: newUrl.value,
    events: newEvents.value
  })
  newUrl.value = ''
  showForm.value = false
  const res = await api.get("/webhook-endpoints")
  endpoints.value = res.data
}

async function deleteEndpoint(id: string) {
  await api.delete("/webhook-endpoints/" + id)
  endpoints.value = endpoints.value.filter(e => e.id !== id)
}
</script>

<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold">Webhook Endpoints</h1>
      <button @click="showForm = !showForm" class="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 text-sm">
        Add Endpoint
      </button>
    </div>
    <div v-if="showForm" class="bg-white rounded-lg shadow p-4 mb-4">
      <div class="space-y-3">
        <input v-model="newUrl" placeholder="https://your-app.com/webhook" class="w-full px-3 py-2 border rounded-md text-sm" />
        <input v-model="newEvents" placeholder="payment_intent.succeeded" class="w-full px-3 py-2 border rounded-md text-sm" />
        <button @click="createEndpoint" class="bg-green-600 text-white px-4 py-2 rounded-md hover:bg-green-700 text-sm">Save</button>
      </div>
    </div>
    <div v-if="loading" class="text-gray-500">Loading...</div>
    <div v-else-if="endpoints.length === 0" class="bg-white rounded-lg shadow p-6 text-gray-500">
      No webhook endpoints configured.
    </div>
    <div v-else class="space-y-3">
      <div v-for="ep in endpoints" :key="ep.id" class="bg-white rounded-lg shadow p-4 flex justify-between items-center">
        <div>
          <p class="font-medium text-sm">{{ ep.url }}</p>
          <p class="text-xs text-gray-500 mt-1">Events: {{ ep.events || "All" }}</p>
          <p class="text-xs text-gray-400">Status: {{ ep.status }} | Created: {{ ep.createdAt?.substring(0, 10) }}</p>
        </div>
        <button @click="deleteEndpoint(ep.id)" class="text-red-600 hover:text-red-800 text-sm">Delete</button>
      </div>
    </div>
  </div>
</template>