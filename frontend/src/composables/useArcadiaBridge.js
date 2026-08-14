import { onBeforeUnmount } from 'vue'
import { fetchProgress, saveProgress } from '../api/progress'

const MSG_SAVE = 'ARCADIA_SAVE'
const MSG_GET_SAVE = 'ARCADIA_GET_SAVE'
const MSG_SAVE_RESPONSE = 'ARCADIA_SAVE_RESPONSE'
const MSG_GET_SAVE_RESPONSE = 'ARCADIA_GET_SAVE_RESPONSE'

function isValidData(data) {
  return data !== null && typeof data === 'object'
}

export function useArcadiaBridge({ getFrame, getGameId, getAllowedOrigin }) {
  let frame = null
  let gameId = null
  let allowedOrigin = ''
  let listenerAttached = false

  function resolveFrame() {
    const candidate = getFrame()
    return candidate?.contentWindow || null
  }

  function postToGame(message) {
    const win = resolveFrame()
    if (!win || !allowedOrigin) return
    win.postMessage(message, allowedOrigin)
  }

  async function handleMessage(event) {
    const win = resolveFrame()
    if (!win || event.source !== win) return
    if (event.origin !== allowedOrigin) return

    const message = event.data
    if (!message || typeof message !== 'object') return
    if (typeof message.messageId !== 'string' || message.messageId.trim() === '') return

    if (message.type === MSG_SAVE) {
      await handleSave(message)
    } else if (message.type === MSG_GET_SAVE) {
      await handleGetSave(message)
    }
  }

  async function handleSave(message) {
    if (!isValidData(message.data)) {
      postToGame({ type: MSG_SAVE_RESPONSE, messageId: message.messageId, success: false, error: 'data must be a JSON object or array' })
      return
    }
    try {
      await saveProgress(gameId, message.data)
      postToGame({ type: MSG_SAVE_RESPONSE, messageId: message.messageId, success: true })
    } catch (error) {
      postToGame({
        type: MSG_SAVE_RESPONSE,
        messageId: message.messageId,
        success: false,
        error: error.response?.data?.message || 'Failed to save progress'
      })
    }
  }

  async function handleGetSave(message) {
    try {
      const progress = await fetchProgress(gameId)
      postToGame({ type: MSG_GET_SAVE_RESPONSE, messageId: message.messageId, success: true, data: progress?.data ?? null })
    } catch (error) {
      if (error.response?.status === 404) {
        postToGame({ type: MSG_GET_SAVE_RESPONSE, messageId: message.messageId, success: true, data: null })
      } else {
        postToGame({
          type: MSG_GET_SAVE_RESPONSE,
          messageId: message.messageId,
          success: false,
          error: error.response?.data?.message || 'Failed to load progress'
        })
      }
    }
  }

  function install() {
    gameId = getGameId()
    allowedOrigin = getAllowedOrigin()
    if (!listenerAttached) {
      window.addEventListener('message', handleMessage)
      listenerAttached = true
    }
  }

  function uninstall() {
    if (listenerAttached) {
      window.removeEventListener('message', handleMessage)
      listenerAttached = false
    }
    frame = null
    gameId = null
  }

  onBeforeUnmount(uninstall)

  return { install, uninstall, postToGame }
}
