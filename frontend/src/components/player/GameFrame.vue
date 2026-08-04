<script setup>
import { ref } from 'vue'

defineProps({
  src: { type: String, required: true },
  title: { type: String, default: 'Juego' }
})

const loading = ref(true)

function onLoad() {
  loading.value = false
}
</script>

<template>
  <div class="game-frame">
    <div v-if="loading" class="frame-loader">
      <span>🎮</span>
      <p>Cargando juego...</p>
    </div>
    <iframe
      :src="src"
      :title="title"
      allow="fullscreen"
      allowfullscreen
      sandbox="allow-scripts allow-same-origin allow-popups"
      @load="onLoad"
    />
  </div>
</template>
