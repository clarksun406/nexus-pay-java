<template>
  <div class="min-h-screen flex">
    <aside class="w-64 bg-gray-900 text-white">
      <div class="p-4 border-b border-gray-800">
        <h1 class="text-xl font-bold">NexusPay</h1>
      </div>
      <nav class="p-4 space-y-2">
        <router-link to="/overview" class="block px-3 py-2 rounded hover:bg-gray-800">Overview</router-link>
        <router-link to="/payments" class="block px-3 py-2 rounded hover:bg-gray-800">Payments</router-link>
        <router-link to="/refunds" class="block px-3 py-2 rounded hover:bg-gray-800">Refunds</router-link>
        <router-link to="/customers" class="block px-3 py-2 rounded hover:bg-gray-800">Customers</router-link>
        <router-link to="/subscriptions" class="block px-3 py-2 rounded hover:bg-gray-800">Subscriptions</router-link>
        <router-link to="/routing/rules" class="block px-3 py-2 rounded hover:bg-gray-800">Routing Rules</router-link>
        <router-link to="/connectors" class="block px-3 py-2 rounded hover:bg-gray-800">Connectors</router-link>
        <router-link to="/payment-links" class="block px-3 py-2 rounded hover:bg-gray-800">Payment Links</router-link>
        <router-link to="/developers/api-keys" class="block px-3 py-2 rounded hover:bg-gray-800">API Keys</router-link>
        <router-link to="/developers/webhooks" class="block px-3 py-2 rounded hover:bg-gray-800">Webhooks</router-link>
        <router-link to="/team" class="block px-3 py-2 rounded hover:bg-gray-800">Team</router-link>
        <router-link to="/settings/security" class="block px-3 py-2 rounded hover:bg-gray-800">Settings</router-link>
      </nav>
    </aside>
    <main class="flex-1 p-8">
      <router-view />
    </main>
  </div>
</template>
