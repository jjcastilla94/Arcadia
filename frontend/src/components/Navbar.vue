<script setup>
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

function logout() {
  auth.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <header v-if="auth.isAuthenticated" class="navbar">
    <span class="brand">🎮 Arcadia</span>
    <nav class="nav-links">
      <router-link to="/">Inicio</router-link>
      <router-link to="/library">Biblioteca</router-link>
      <router-link v-if="auth.roleNames.includes('ROLE_ADMIN')" to="/admin">Admin</router-link>
    </nav>
    <div class="nav-user">
      <span class="nickname">{{ auth.user?.nickname }}</span>
      <button class="btn btn-outline" type="button" @click="logout">Salir</button>
    </div>
  </header>
</template>
