<script setup>
defineProps({
  game: { type: Object, required: true }
})
</script>

<template>
  <article class="game-card">
    <div class="game-thumb">
      <img v-if="game.thumbnailPath" :src="game.thumbnailPath" :alt="game.title" />
      <div v-else class="game-thumb-placeholder">🎮</div>
      <span v-if="game.playCount > 0" class="game-plays">{{ game.playCount }} jugadas</span>
    </div>
    <div class="game-info">
      <h3 class="game-title">{{ game.title }}</h3>
      <div class="game-meta">
        <span v-if="game.category" class="game-category">{{ game.category.name }}</span>
        <span class="game-version">v{{ game.version }}</span>
      </div>
    </div>
  </article>
</template>
