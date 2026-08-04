import { ref } from 'vue'
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
    const body = new Blob([JSON.stringify({ sessionId: id })], { type: 'application/json' })
    navigator.sendBeacon('/api/play-sessions/end', body)
  }

  return { sessionId, sessionActive, sessionError, begin, finish, beaconFinish }
}
