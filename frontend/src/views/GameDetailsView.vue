<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchGame } from '../api/games'
import { addToLibrary, isInLibrary, removeFromLibrary } from '../api/library'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const game = ref(null)
const loading = ref(true)
const error = ref('')
const notice = ref('')
const inLibrary = ref(false)
const libraryBusy = ref(false)

function formatSize(bytes) {
  if (bytes == null) return '—'
  if (bytes >= 1024 * 1024) return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
  if (bytes >= 1024) return `${(bytes / 1024).toFixed(0)} KB`
  return `${bytes} B`
}

function formatDate(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleDateString('es-ES', { year: 'numeric', month: 'long', day: 'numeric' })
}

function formatDuration(seconds) {
  if (!seconds) return '0 min'
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  return hours > 0 ? `${hours}h ${minutes}m` : `${minutes} min`
}

function play() {
  if (!auth.isAuthenticated) {
    router.push({ name: 'login', query: { redirect: `/play/${game.value.slug}` } })
    return
  }
  router.push({ name: 'player', params: { slug: game.value.slug } })
}

async function toggleLibrary() {
  if (!auth.isAuthenticated) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  libraryBusy.value = true
  notice.value = ''
  try {
    if (inLibrary.value) {
      await removeFromLibrary(game.value.id)
      inLibrary.value = false
      notice.value = 'Eliminado de tu biblioteca.'
    } else {
      await addToLibrary(game.value.id)
      inLibrary.value = true
      notice.value = 'Añadido a tu biblioteca.'
    }
  } catch {
    notice.value = 'No se pudo actualizar tu biblioteca.'
  } finally {
    libraryBusy.value = false
  }
}

onMounted(async () => {
  try {
    game.value = await fetchGame(route.params.slug)
    if (auth.isAuthenticated) {
      inLibrary.value = await isInLibrary(game.value.id)
    }
  } catch {
    error.value = 'No se encontró el juego.'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="game-details">
    <router-link to="/" class="back-link">← Volver al catálogo</router-link>

    <p v-if="loading" class="muted">Cargando juego...</p>

    <div v-else-if="error" class="details-error">
      <p class="error">{{ error }}</p>
      <router-link to="/" class="btn btn-outline">Ir al catálogo</router-link>
    </div>

    <template v-else>
      <section class="details-cover">
        <img v-if="game.coverUrl" :src="game.coverUrl" :alt="game.title" class="details-cover-img" />
        <div v-else class="details-cover-fallback">🎮</div>
        <div class="details-cover-overlay">
          <span v-if="game.category" class="game-category">{{ game.category.name }}</span>
          <h1>{{ game.title }}</h1>
        </div>
      </section>

      <section class="details-actions">
        <button type="button" class="btn btn-primary btn-lg" @click="play">▶ Jugar</button>
        <button
          type="button"
          class="btn btn-lg"
          :class="inLibrary ? 'btn-success' : 'btn-outline'"
          :disabled="libraryBusy"
          @click="toggleLibrary"
        >
          {{ inLibrary ? '✓ En tu biblioteca' : '+ Añadir a biblioteca' }}
        </button>
        <p v-if="notice" class="notice">{{ notice }}</p>
      </section>

      <section class="details-description">
        <h2>Descripción</h2>
        <p>{{ game.description || 'Sin descripción disponible.' }}</p>
      </section>

      <section class="details-section">
        <h2>Capturas</h2>
        <p v-if="game.images.length === 0" class="muted">Este juego aún no tiene capturas.</p>
        <div v-else class="captures-grid">
          <img
            v-for="image in game.images"
            :key="image.id"
            :src="image.imageUrl"
            :alt="`${game.title} captura`"
            class="capture"
          />
        </div>
      </section>

      <section class="details-section">
        <h2>Información</h2>
        <div class="info-grid">
          <div class="info-item"><span>Categoría</span><strong>{{ game.category?.name || 'Sin categoría' }}</strong></div>
          <div class="info-item"><span>Versión</span><strong>{{ game.version }}</strong></div>
          <div class="info-item"><span>Tamaño</span><strong>{{ formatSize(game.fileSize) }}</strong></div>
          <div class="info-item"><span>Publicado</span><strong>{{ formatDate(game.createdAt) }}</strong></div>
          <div class="info-item"><span>Partidas</span><strong>{{ game.playCount }}</strong></div>
          <div class="info-item"><span>Jugadores</span><strong>{{ game.playerCount }}</strong></div>
          <div class="info-item"><span>Tiempo jugado</span><strong>{{ formatDuration(game.totalPlayTimeSeconds) }}</strong></div>
        </div>
      </section>

      <section class="details-section">
        <h2>Logros</h2>
        <p v-if="game.achievements.length === 0" class="muted">Este juego aún no tiene logros.</p>
        <div v-else class="achievements-grid">
          <div v-for="achievement in game.achievements" :key="achievement.id" class="achievement-card">
            <span class="achievement-icon">{{ achievement.icon || '🏆' }}</span>
            <div>
              <strong>{{ achievement.title }}</strong>
              <p>{{ achievement.description }}</p>
            </div>
            <span class="achievement-points">{{ achievement.points }} pts</span>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>
