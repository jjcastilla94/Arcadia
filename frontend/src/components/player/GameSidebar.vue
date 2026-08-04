<script setup>
import { formatClock } from '../../utils/format'

defineProps({
  game: { type: Object, required: true },
  elapsedSeconds: { type: Number, required: true },
  sessionError: { type: String, default: '' }
})

defineEmits(['exit'])
</script>

<template>
  <aside class="player-sidebar">
    <h2>{{ game.title }}</h2>
    <p v-if="game.category" class="game-category">{{ game.category.name }}</p>

    <div class="sidebar-time">
      <span>Tiempo en esta sesión</span>
      <strong>{{ formatClock(elapsedSeconds) }}</strong>
    </div>

    <p v-if="sessionError" class="error">{{ sessionError }}</p>

    <button type="button" class="btn btn-outline" @click="$emit('exit')">← Salir del juego</button>
  </aside>
</template>
