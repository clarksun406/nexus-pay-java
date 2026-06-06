<script setup lang="ts">
import { ref } from 'vue'

const reportTypes = [
  { id: "revenue", name: "Revenue Report", desc: "Payment volume and fees over time" },
  { id: "providers", name: "Provider Performance", desc: "Success rates and latency by provider" },
  { id: "disputes", name: "Dispute Summary", desc: "Dispute win/loss rates and trends" },
  { id: "payouts", name: "Payout Report", desc: "Scheduled and completed payouts" },
  { id: "customers", name: "Customer Growth", desc: "New customers and churn rate" },
]
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">Reports</h1>
    <div class="grid grid-cols-2 gap-4">
      <div v-for="r in reportTypes" :key="r.id" class="bg-white rounded-lg shadow p-6 hover:shadow-md cursor-pointer">
        <h3 class="font-semibold text-lg">{{ r.name }}</h3>
        <p class="text-sm text-gray-500 mt-1">{{ r.desc }}</p>
        <button class="mt-4 text-blue-600 hover:text-blue-800 text-sm font-medium">Generate Report</button>
      </div>
    </div>
  </div>
</template>