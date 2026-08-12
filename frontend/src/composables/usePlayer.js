import { ref } from 'vue'
import { useAuthStore } from '../stores/auth'
import { endSession, startSession } from '../api/sessions'

export function usePlayer() {
  const sessionId = ref(null)
  const sessionActive = ref(false)
  const sessionError = ref('')

  async function begin(gameId) {
    try {
      const session = await startSession(gameId)
      sessionId.value = session.id
      sessionActive.value = true
    } catch {
      sessionError.value = 'No se pudo iniciar la sesión de juego.'
    }
  }

  async function finish() {
    const id = sessionId.value
    if (!id || !sessionActive.value) return
    sessionActive.value = false
    try {
      await endSession(id)
    } catch {
      // el cierre no debe romper la navegación
    }
  }

  function beaconFinish() {
    const id = sessionId.value
    if (!id || !sessionActive.value) return
    sessionActive.value = false
    const token = useAuthStore().accessToken
    if (!token) return
    fetch('/api/play-sessions/end', {
      method: 'POST',
      keepalive: true,
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ sessionId: id })
    }).catch(() => {})
  }

  return { sessionId, sessionActive, sessionError, begin, finish, beaconFinish }
}
