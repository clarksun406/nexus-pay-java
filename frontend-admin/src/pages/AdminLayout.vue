<template>
  <div class="min-h-screen bg-gray-100 flex">
    <!-- Sidebar -->
    <aside class="w-64 bg-slate-900 text-white">
      <div class="p-4 border-b border-slate-700">
        <h1 class="text-lg font-bold">NexusPay Admin</h1>
      </div>
      <nav class="p-4">
        <ul class="space-y-2">
          <li v-for="item in menu" :key="item.path">
            <router-link :to="item.path"
                         class="flex items-center gap-3 px-3 py-2 rounded hover:bg-slate-800"
                         :class="{ 'bg-slate-800': isActive(item.path) }">
              <span v-html="item.icon"></span>
              {{ item.label }}
            </router-link>
          </li>
        </ul>
      </nav>
    </aside>

    <!-- Main Content -->
    <main class="flex-1 p-6">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()

const menu = [
  {
    path: '/admin',
    label: 'Overview',
    icon: '<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"></path></svg>'
  },
  {
    path: '/admin/organizations',
    label: 'Organizations',
    icon: '<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path></svg>'
  }
]

function isActive(path) {
  return route.path === path || (path !== '/admin' && route.path.startsWith(path))
}
</script>
