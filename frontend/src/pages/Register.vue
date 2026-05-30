<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-100">
    <div class="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
      <h1 class="text-2xl font-bold mb-6 text-center">Create Account</h1>
      <form @submit.prevent="handleSubmit">
        <div class="mb-4">
          <label class="block text-sm font-medium mb-1">Email</label>
          <input v-model="form.email" type="email" required class="w-full px-3 py-2 border rounded-lg" />
        </div>
        <div class="mb-4">
          <label class="block text-sm font-medium mb-1">Password</label>
          <input v-model="form.password" type="password" required class="w-full px-3 py-2 border rounded-lg" />
        </div>
        <div class="mb-4">
          <label class="block text-sm font-medium mb-1">Organization Name</label>
          <input v-model="form.organizationName" required class="w-full px-3 py-2 border rounded-lg" />
        </div>
        <div class="mb-6">
          <label class="block text-sm font-medium mb-1">Merchant Name</label>
          <input v-model="form.merchantName" required class="w-full px-3 py-2 border rounded-lg" />
        </div>
        <button type="submit" class="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700">
          Create Account
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const form = reactive({ email: '', password: '', organizationName: '', merchantName: '' })

async function handleSubmit() {
  await auth.register(form)
  router.push('/overview')
}
</script>
