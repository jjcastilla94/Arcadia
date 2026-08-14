<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const identifier = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

const redirectPath =
  typeof route.query.redirect === 'string' && route.query.redirect.startsWith('/')
    ? route.query.redirect
    : null

function extractError(e) {
  const data = e.response?.data
  if (data?.message) return data.message
  if (data?.fieldErrors) return Object.values(data.fieldErrors)[0]
  return 'No se pudo conectar con el servidor'
}

async function submit() {
  error.value = ''
  loading.value = true
  try {
    await auth.login({ identifier: identifier.value.trim(), password: password.value })
    router.push(redirectPath || { name: 'home' })
  } catch (e) {
    error.value = extractError(e)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <form class="auth-card" @submit.prevent="submit">
      <h1>Iniciar sesión</h1>
      <p class="subtitle">Accede a tu cuenta de Arcadia</p>

      <label>
        Email o nickname
        <input v-model="identifier" type="text" required autocomplete="username" />
      </label>

      <label>
        Contraseña
        <input v-model="password" type="password" required autocomplete="current-password" />
      </label>

      <p v-if="error" class="error">{{ error }}</p>

      <button class="btn btn-primary" type="submit" :disabled="loading">
        {{ loading ? 'Entrando...' : 'Entrar' }}
      </button>

      <p class="switch">
        ¿No tienes cuenta? <router-link to="/register">Regístrate</router-link>
      </p>
    </form>
  </div>
</template>
