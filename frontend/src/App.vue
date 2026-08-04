<script setup>
import Navbar from './components/Navbar.vue'
</script>

<template>
  <Navbar />
  <main :class="{ fullwidth: $route.meta.fullWidth }">
    <router-view />
  </main>
</template>
