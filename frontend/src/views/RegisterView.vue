<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const nickname = ref('')
const email = ref('')
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
    await auth.register({
      nickname: nickname.value.trim(),
      email: email.value.trim(),
      password: password.value
    })
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
      <h1>Crear cuenta</h1>
      <p class="subtitle">Únete a Arcadia</p>

      <label>
        Nickname
        <input v-model="nickname" type="text" required minlength="3" maxlength="50" autocomplete="nickname" />
      </label>

      <label>
        Email
        <input v-model="email" type="email" required autocomplete="email" />
      </label>

      <label>
        Contraseña
        <input v-model="password" type="password" required minlength="8" autocomplete="new-password" />
      </label>

      <p v-if="error" class="error">{{ error }}</p>

      <button class="btn btn-primary" type="submit" :disabled="loading">
        {{ loading ? 'Creando...' : 'Registrarse' }}
      </button>

      <p class="switch">
        ¿Ya tienes cuenta? <router-link to="/login">Inicia sesión</router-link>
      </p>
    </form>
  </div>
</template>
