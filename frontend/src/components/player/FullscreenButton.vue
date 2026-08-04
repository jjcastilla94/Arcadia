<script setup>
import { onMounted, onUnmounted, ref } from 'vue'

const isFullscreen = ref(false)
const root = ref(null)

function toggle() {
  if (!document.fullscreenElement) {
    root.value.requestFullscreen?.().catch(() => {})
  } else {
    document.exitFullscreen?.().catch(() => {})
  }
}

function onFullscreenChange() {
  isFullscreen.value = Boolean(document.fullscreenElement)
}

onMounted(() => document.addEventListener('fullscreenchange', onFullscreenChange))
onUnmounted(() => document.removeEventListener('fullscreenchange', onFullscreenChange))
</script>

<template>
  <div ref="root" class="fullscreen-wrap">
    <slot />
    <button type="button" class="btn btn-outline btn-sm fullscreen-btn" @click="toggle">
      {{ isFullscreen ? '⛶ Salir de pantalla completa' : '⛶ Pantalla completa' }}
    </button>
  </div>
</template>
