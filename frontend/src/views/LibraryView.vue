<script setup>
import { onMounted, ref } from 'vue'
import {
  fetchLibrary,
  removeFromLibrary,
  setLibraryRating,
  setLibraryStatus
} from '../api/library'

const STATUS_LABELS = {
  PLAYING: 'En curso',
  COMPLETED: 'Completado',
  ABANDONED: 'Abandonado'
}

const items = ref([])
const loading = ref(true)
const error = ref('')

function formatDuration(seconds) {
  if (!seconds) return '0 min'
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  return hours > 0 ? `${hours}h ${minutes}m` : `${minutes} min`
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    items.value = await fetchLibrary()
  } catch {
    error.value = 'No se pudo cargar tu biblioteca.'
  } finally {
    loading.value = false
  }
}

async function changeStatus(item, event) {
  try {
    await setLibraryStatus(item.game.id, event.target.value)
    item.status = event.target.value
  } catch {
    error.value = 'No se pudo actualizar el estado.'
  }
}

async function rate(item, rating) {
  if (item.rating === rating) {
    rating = null
  }
  try {
    await setLibraryRating(item.game.id, rating)
    item.rating = rating
  } catch {
    error.value = 'No se pudo actualizar la valoración.'
  }
}

async function remove(item) {
  try {
    await removeFromLibrary(item.game.id)
    items.value = items.value.filter((i) => i.id !== item.id)
  } catch {
    error.value = 'No se pudo eliminar el juego.'
  }
}

onMounted(load)
</script>

<template>
  <div class="library">
    <h1>Mi biblioteca</h1>
    <p class="subtitle">Tu colección personal de juegos.</p>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-else-if="loading" class="muted">Cargando tu biblioteca...</p>

    <template v-else-if="items.length === 0">
      <p class="muted">Tu biblioteca está vacía.</p>
      <router-link to="/" class="btn btn-primary">Explorar el catálogo</router-link>
    </template>

    <div v-else class="library-grid">
      <article v-for="item in items" :key="item.id" class="library-card">
        <router-link :to="`/games/${item.game.slug}`" class="library-thumb-link">
          <div class="library-thumb">
            <img v-if="item.game.thumbnailPath" :src="item.game.thumbnailPath" :alt="item.game.title" />
            <div v-else class="game-thumb-placeholder">🎮</div>
          </div>
          <div class="library-title">
            <strong>{{ item.game.title }}</strong>
            <span v-if="item.game.category" class="game-category">{{ item.game.category.name }}</span>
          </div>
        </router-link>

        <div class="library-controls">
          <select class="status-select" :value="item.status" @change="changeStatus(item, $event)">
            <option v-for="(label, value) in STATUS_LABELS" :key="value" :value="value">{{ label }}</option>
          </select>

          <div class="stars" :aria-label="`Valoración: ${item.rating || 0} de 5`">
            <button
              v-for="star in 5"
              :key="star"
              type="button"
              class="star"
              :class="{ filled: item.rating >= star }"
              @click="rate(item, star)"
            >
              ★
            </button>
          </div>

          <span class="library-time">{{ formatDuration(item.timePlayedSeconds) }}</span>

          <button type="button" class="btn btn-outline btn-sm" @click="remove(item)">Quitar</button>
        </div>
      </article>
    </div>
  </div>
</template>
