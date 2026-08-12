<script setup>
import { ref, computed } from 'vue'
import { useAuthStore } from '../stores/auth'
import { updateProfile, changePassword } from '../api/user'

const auth = useAuthStore()

const user = computed(() => auth.user)

const nickname = ref(auth.user?.nickname || '')
const avatarUrl = ref(auth.user?.avatarUrl || '')

const savingProfile = ref(false)
const profileMsg = ref('')
const profileError = ref('')

const currentPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')

const savingPassword = ref(false)
const passwordMsg = ref('')
const passwordError = ref('')

function extractError(e) {
  const data = e.response?.data
  if (data?.fieldErrors) return Object.values(data.fieldErrors)[0]
  if (data?.message) return data.message
  return 'No se pudo conectar con el servidor'
}

async function saveProfile() {
  profileMsg.value = ''
  profileError.value = ''
  savingProfile.value = true
  try {
    const payload = { nickname: nickname.value.trim() || null, avatarUrl: avatarUrl.value.trim() || null }
    const res = await updateProfile(payload)
    auth.setUser(res.data.data)
    nickname.value = auth.user.nickname
    avatarUrl.value = auth.user.avatarUrl || ''
    profileMsg.value = 'Perfil actualizado'
  } catch (e) {
    profileError.value = extractError(e)
  } finally {
    savingProfile.value = false
  }
}

async function savePassword() {
  passwordMsg.value = ''
  passwordError.value = ''
  if (newPassword.value !== confirmPassword.value) {
    passwordError.value = 'Las contraseñas no coinciden'
    return
  }
  savingPassword.value = true
  try {
    await changePassword({
      currentPassword: currentPassword.value,
      newPassword: newPassword.value
    })
    currentPassword.value = ''
    newPassword.value = ''
    confirmPassword.value = ''
    passwordMsg.value = 'Contraseña cambiada correctamente'
  } catch (e) {
    passwordError.value = extractError(e)
  } finally {
    savingPassword.value = false
  }
}
</script>

<template>
  <div class="profile-page">
    <h1>Perfil</h1>

    <div class="profile-header">
      <img
        v-if="user?.avatarUrl"
        class="avatar"
        :src="user.avatarUrl"
        alt="Avatar"
      />
      <div v-else class="avatar avatar-fallback">👤</div>
      <div>
        <h2>{{ user?.nickname }}</h2>
        <p class="muted">{{ user?.email }}</p>
      </div>
    </div>

    <div class="card">
      <h3>Configuración</h3>
      <form class="profile-form" @submit.prevent="saveProfile">
        <label>
          Nickname
          <input v-model="nickname" type="text" minlength="3" maxlength="50" required autocomplete="nickname" />
        </label>

        <label>
          Avatar URL
          <input v-model="avatarUrl" type="url" maxlength="255" placeholder="https://..." />
        </label>

        <p v-if="profileError" class="error">{{ profileError }}</p>
        <p v-if="profileMsg" class="success">{{ profileMsg }}</p>

        <button class="btn btn-primary" type="submit" :disabled="savingProfile">
          {{ savingProfile ? 'Guardando...' : 'Guardar cambios' }}
        </button>
      </form>
    </div>

    <div class="card">
      <h3>Cambiar contraseña</h3>
      <form class="profile-form" @submit.prevent="savePassword">
        <label>
          Actual
          <input v-model="currentPassword" type="password" required autocomplete="current-password" />
        </label>

        <label>
          Nueva
          <input v-model="newPassword" type="password" required minlength="8" maxlength="100" autocomplete="new-password" />
        </label>

        <label>
          Confirmar
          <input v-model="confirmPassword" type="password" required minlength="8" maxlength="100" autocomplete="new-password" />
        </label>

        <p v-if="passwordError" class="error">{{ passwordError }}</p>
        <p v-if="passwordMsg" class="success">{{ passwordMsg }}</p>

        <button class="btn btn-primary" type="submit" :disabled="savingPassword">
          {{ savingPassword ? 'Cambiando...' : 'Cambiar contraseña' }}
        </button>
      </form>
    </div>
  </div>
</template>
