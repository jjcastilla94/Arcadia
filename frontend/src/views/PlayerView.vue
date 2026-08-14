<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchGame } from '../api/games'
import FullscreenButton from '../components/player/FullscreenButton.vue'
import GameFrame from '../components/player/GameFrame.vue'
import GameSidebar from '../components/player/GameSidebar.vue'
import { useArcadiaBridge } from '../composables/useArcadiaBridge'
import { usePlayer } from '../composables/usePlayer'
import { usePlayTimer } from '../composables/usePlayTimer'

const route = useRoute()
const router = useRouter()

const game = ref(null)
const loading = ref(true)
const error = ref('')
const gameFrame = ref(null)

const { elapsedSeconds, start } = usePlayTimer()
const player = usePlayer()
const { sessionError } = player

const iframeSrc = computed(() =>
  game.value ? game.value.filePath || `/uploads/games/${game.value.slug}/index.html` : ''
)

const allowedOrigin = computed(() => window.location.origin)

const arcadiaBridge = useArcadiaBridge({
  getFrame: () => gameFrame.value?.iframeEl,
  getGameId: () => game.value?.id,
  getAllowedOrigin: () => allowedOrigin.value
})

async function exit() {
  await player.finish()
  router.push({ name: 'game-details', params: { slug: game.value.slug } })
}

onMounted(async () => {
  try {
    game.value = await fetchGame(route.params.slug)
    await player.begin(game.value.id)
    start()
    arcadiaBridge.install()
  } catch {
    error.value = 'No se pudo cargar el juego.'
  } finally {
    loading.value = false
  }
  window.addEventListener('pagehide', player.beaconFinish)
})

onBeforeUnmount(() => {
  window.removeEventListener('pagehide', player.beaconFinish)
  arcadiaBridge.uninstall()
  player.finish()
})
</script>

<template>
  <div class="player-page">
    <p v-if="loading" class="muted">Preparando el juego...</p>

    <div v-else-if="error" class="details-error">
      <p class="error">{{ error }}</p>
      <router-link :to="`/games/${route.params.slug}`" class="btn btn-outline">Volver al detalle</router-link>
    </div>

    <template v-else>
      <div class="player-main">
        <FullscreenButton>
          <GameFrame ref="gameFrame" :src="iframeSrc" :title="game.title" />
        </FullscreenButton>
      </div>

      <GameSidebar
        :game="game"
        :elapsed-seconds="elapsedSeconds"
        :session-error="sessionError"
        @exit="exit"
      />
    </template>
  </div>
</template>
