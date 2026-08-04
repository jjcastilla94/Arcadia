<script setup>
import { onMounted, reactive, ref } from 'vue'
import { createGame, deleteGame, fetchAdminGames, updateGame } from '../api/admin'
import { fetchCategories } from '../api/games'

const games = ref([])
const categories = ref([])
const loading = ref(true)
const error = ref('')
const notice = ref('')
const showCreate = ref(false)

const emptyForm = () => ({ title: '', description: '', categoryId: '', version: '', releaseNotes: '', isPublic: true })
const form = reactive(emptyForm())
const zipFile = ref(null)
const thumbnailFile = ref(null)
const coverFile = ref(null)

const editingId = ref(null)
const editForm = reactive({ ...emptyForm() })
const editZipFile = ref(null)
const editThumbnailFile = ref(null)
const editCoverFile = ref(null)

function buildData(formData, zip, thumb, cover) {
  const fd = new FormData()
  fd.append('title', formData.title)
  fd.append('description', formData.description || '')
  if (formData.categoryId) fd.append('categoryId', String(formData.categoryId))
  if (formData.version) fd.append('version', formData.version)
  fd.append('releaseNotes', formData.releaseNotes || '')
  fd.append('isPublic', String(formData.isPublic))
  if (zip.value) fd.append('zip', zip.value)
  if (thumb.value) fd.append('thumbnail', thumb.value)
  if (cover.value) fd.append('cover', cover.value)
  return fd
}

function toForm(payload) {
  const fd = new FormData()
  for (const [key, value] of Object.entries(payload)) {
    if (value !== undefined && value !== null && value !== '') {
      fd.append(key, String(value))
    }
  }
  return fd
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    games.value = await fetchAdminGames()
  } catch {
    error.value = 'No se pudo cargar la lista de juegos.'
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    categories.value = await fetchCategories()
  } catch {
    categories.value = []
  }
}

async function submitCreate() {
  error.value = ''
  notice.value = ''
  try {
    await createGame(buildData(form, zipFile, thumbnailFile, coverFile))
    Object.assign(form, emptyForm())
    zipFile.value = null
    thumbnailFile.value = null
    coverFile.value = null
    showCreate.value = false
    notice.value = 'Juego subido correctamente.'
    await load()
  } catch (e) {
    error.value = e.response?.data?.message || 'No se pudo subir el juego.'
  }
}

function startEdit(game) {
  editingId.value = game.id
  Object.assign(editForm, {
    title: game.title,
    description: game.description || '',
    categoryId: game.category?.id || '',
    version: '',
    releaseNotes: '',
    isPublic: game.isPublic
  })
  editZipFile.value = null
  editThumbnailFile.value = null
  editCoverFile.value = null
}

async function submitEdit(game) {
  error.value = ''
  notice.value = ''
  try {
    await updateGame(game.id, buildData(editForm, editZipFile, editThumbnailFile, editCoverFile))
    editingId.value = null
    notice.value = 'Juego actualizado.'
    await load()
  } catch (e) {
    error.value = e.response?.data?.message || 'No se pudo actualizar el juego.'
  }
}

async function toggleVisibility(game, field) {
  error.value = ''
  const payload = { isPublic: game.isPublic, isHidden: game.isHidden }
  payload[field] = !game[field]
  try {
    await updateGame(game.id, toForm(payload))
    await load()
  } catch (e) {
    error.value = e.response?.data?.message || 'No se pudo actualizar el juego.'
  }
}

async function removeGame(game) {
  if (!window.confirm(`¿Borrar definitivamente "${game.title}"?`)) return
  error.value = ''
  try {
    await deleteGame(game.id)
    await load()
  } catch (e) {
    error.value = e.response?.data?.message || 'No se pudo borrar el juego.'
  }
}

onMounted(() => {
  load()
  loadCategories()
})
</script>

<template>
  <div class="admin">
    <div class="admin-header">
      <div>
        <h1>Panel de administración</h1>
        <p class="subtitle">Sube, edita y publica juegos HTML5.</p>
      </div>
      <button class="btn btn-primary" type="button" @click="showCreate = !showCreate">
        {{ showCreate ? 'Cancelar' : '+ Nuevo juego' }}
      </button>
    </div>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-if="notice" class="notice">{{ notice }}</p>

    <form v-if="showCreate" class="admin-form card" @submit.prevent="submitCreate">
      <h3>Subir nuevo juego</h3>
      <div class="admin-form-grid">
        <label>Título *
          <input v-model.trim="form.title" required placeholder="Nombre del juego" />
        </label>
        <label>Categoría
          <select v-model="form.categoryId" class="status-select">
            <option value="">— Sin categoría —</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
          </select>
        </label>
        <label class="admin-span-2">Descripción
          <textarea v-model.trim="form.description" rows="3" placeholder="¿De qué va el juego?"></textarea>
        </label>
        <label>Versión
          <input v-model.trim="form.version" placeholder="1.0" />
        </label>
        <label>Notas de versión
          <input v-model.trim="form.releaseNotes" placeholder="Qué cambia en esta versión" />
        </label>
        <label class="admin-span-2">Juego (ZIP con index.html en la raíz) *
          <input type="file" accept=".zip" @change="zipFile = $event.target.files[0] || null" />
        </label>
        <label>Miniatura
          <input type="file" accept="image/*" @change="thumbnailFile = $event.target.files[0] || null" />
        </label>
        <label>Portada
          <input type="file" accept="image/*" @change="coverFile = $event.target.files[0] || null" />
        </label>
      </div>
      <label class="admin-check">
        <input v-model="form.isPublic" type="checkbox" />
        Publicar en el catálogo
      </label>
      <div class="admin-actions">
        <button class="btn btn-success" type="submit" :disabled="!zipFile">Subir juego</button>
        <button class="btn btn-outline" type="button" @click="showCreate = false">Cancelar</button>
      </div>
    </form>

    <p v-else-if="loading" class="muted">Cargando juegos...</p>

    <template v-else-if="games.length === 0">
      <p class="muted">Aún no hay juegos. Sube el primero.</p>
    </template>

    <div v-else class="admin-table">
      <article v-for="game in games" :key="game.id" class="admin-row">
        <div class="admin-cell admin-cell-thumb">
          <div class="admin-thumb">
            <img v-if="game.thumbnailPath" :src="game.thumbnailPath" :alt="game.title" />
            <div v-else class="game-thumb-placeholder">🎮</div>
          </div>
          <div class="admin-cell-info">
            <strong>{{ game.title }}</strong>
            <span class="muted">{{ game.slug }} · v{{ game.version }}</span>
          </div>
        </div>
        <div class="admin-cell">
          <span v-if="game.category" class="game-category">{{ game.category.name }}</span>
          <span v-else class="muted">—</span>
        </div>
        <div class="admin-cell admin-cell-status">
          <span class="badge" :class="game.isPublic ? 'badge-public' : 'badge-draft'">
            {{ game.isPublic ? 'Público' : 'Borrador' }}
          </span>
          <span v-if="game.isHidden" class="badge badge-hidden">Oculto</span>
        </div>

        <div v-if="editingId === game.id" class="admin-edit">
          <div class="admin-form-grid">
            <label>Título
              <input v-model.trim="editForm.title" />
            </label>
            <label>Categoría
              <select v-model="editForm.categoryId" class="status-select">
                <option value="">— Sin categoría —</option>
                <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
              </select>
            </label>
            <label class="admin-span-2">Descripción
              <textarea v-model.trim="editForm.description" rows="3"></textarea>
            </label>
            <label>Nueva versión (si subes ZIP)
              <input v-model.trim="editForm.version" placeholder="1.1" />
            </label>
            <label>Notas de versión
              <input v-model.trim="editForm.releaseNotes" />
            </label>
            <label class="admin-span-2">Juego (ZIP con index.html)
              <input type="file" accept=".zip" @change="editZipFile = $event.target.files[0] || null" />
            </label>
            <label>Miniatura
              <input type="file" accept="image/*" @change="editThumbnailFile = $event.target.files[0] || null" />
            </label>
            <label>Portada
              <input type="file" accept="image/*" @change="editCoverFile = $event.target.files[0] || null" />
            </label>
          </div>
          <label class="admin-check">
            <input v-model="editForm.isPublic" type="checkbox" />
            Publicado en el catálogo
          </label>
          <div class="admin-actions">
            <button class="btn btn-success" type="button" @click="submitEdit(game)">Guardar</button>
            <button class="btn btn-outline" type="button" @click="editingId = null">Cancelar</button>
          </div>
        </div>

        <div v-else class="admin-cell admin-cell-actions">
          <button class="btn btn-outline btn-sm" type="button" @click="startEdit(game)">Editar</button>
          <button
            class="btn btn-outline btn-sm"
            type="button"
            @click="toggleVisibility(game, 'isPublic')"
          >
            {{ game.isPublic ? 'Ocultar' : 'Publicar' }}
          </button>
          <button
            class="btn btn-outline btn-sm"
            type="button"
            @click="toggleVisibility(game, 'isHidden')"
          >
            {{ game.isHidden ? 'Mostrar' : 'Ocultar (soft)' }}
          </button>
          <button class="btn btn-danger btn-sm" type="button" @click="removeGame(game)">Borrar</button>
        </div>
      </article>
    </div>
  </div>
</template>
