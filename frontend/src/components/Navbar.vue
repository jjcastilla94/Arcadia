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
  <header class="navbar">
    <span class="brand">🎮 Arcadia</span>
    <nav class="nav-links">
      <router-link to="/">Inicio</router-link>
      <template v-if="auth.isAuthenticated">
        <router-link to="/library">Biblioteca</router-link>
        <router-link to="/profile">Perfil</router-link>
        <router-link v-if="auth.roleNames.includes('ROLE_ADMIN')" to="/admin">Admin</router-link>
      </template>
    </nav>
    <div v-if="auth.isAuthenticated" class="nav-user">
      <span class="nickname">{{ auth.user?.nickname }}</span>
      <button class="btn btn-outline" type="button" @click="logout">Salir</button>
    </div>
    <div v-else class="nav-user">
      <router-link to="/login" class="btn btn-outline">Iniciar sesión</router-link>
      <router-link to="/register" class="btn btn-primary">Registrarse</router-link>
    </div>
  </header>
</template>
