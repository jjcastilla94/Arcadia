<script setup>
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
</script>

<template>
  <div class="home">
    <h1>Hola, {{ auth.user?.nickname }} 👋</h1>
    <p class="subtitle">Bienvenido a Arcadia. Tu sesión está activa.</p>

    <div class="card">
      <h3>Tu perfil</h3>
      <p><strong>Nickname:</strong> {{ auth.user?.nickname }}</p>
      <p><strong>Email:</strong> {{ auth.user?.email }}</p>
      <p><strong>Email verificado:</strong> {{ auth.user?.emailVerified ? 'Sí' : 'No' }}</p>
      <p><strong>Roles:</strong> {{ auth.roleNames.join(', ') || 'Ninguno' }}</p>
    </div>
  </div>
</template>
