<script setup>
import { onMounted, ref } from 'vue'
import { fetchCategories, fetchGames } from '../api/games'
import GameCard from '../components/GameCard.vue'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const games = ref([])
const categories = ref([])
const search = ref('')
const activeCategory = ref(null)
const loading = ref(false)
const error = ref('')

let debounceTimer

async function load() {
  loading.value = true
  error.value = ''
  try {
    games.value = await fetchGames({
      search: search.value,
      category: activeCategory.value?.slug
    })
  } catch {
    error.value = 'No se pudo cargar el catálogo. Inténtalo de nuevo.'
  } finally {
    loading.value = false
  }
}

function onSearch() {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(load, 300)
}

function selectCategory(category) {
  if (activeCategory.value?.id === category?.id) {
    activeCategory.value = null
  } else {
    activeCategory.value = category
  }
  load()
}

onMounted(async () => {
  try {
    categories.value = await fetchCategories()
  } catch {
    // el catálogo funciona aunque fallen los filtros
  }
  load()
})
</script>

<template>
  <div class="home catalog">
    <h1>Hola, {{ auth.user?.nickname }} 👋</h1>
    <p class="subtitle">Elige un juego y juega al instante.</p>

    <div class="catalog-toolbar">
      <input
        v-model="search"
        type="search"
        placeholder="Buscar juegos..."
        class="search-input"
        @input="onSearch"
      />
      <div class="category-chips">
        <button
          type="button"
          class="chip"
          :class="{ active: activeCategory === null }"
          @click="selectCategory(null)"
        >
          Todos
        </button>
        <button
          v-for="category in categories"
          :key="category.id"
          type="button"
          class="chip"
          :class="{ active: activeCategory?.id === category.id }"
          @click="selectCategory(category)"
        >
          {{ category.name }}
        </button>
      </div>
    </div>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-else-if="loading" class="muted">Cargando juegos...</p>
    <p v-else-if="games.length === 0" class="muted">
      No hay juegos en el catálogo todavía.
    </p>

    <div v-else class="game-grid">
      <GameCard v-for="game in games" :key="game.id" :game="game" />
    </div>
  </div>
</template>
