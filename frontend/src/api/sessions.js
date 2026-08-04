import http from './http'

export async function startSession(gameId) {
  const { data } = await http.post('/play-sessions/start', { gameId })
  return data.data
}

export async function endSession(sessionId) {
  const { data } = await http.post('/play-sessions/end', { sessionId })
  return data.data
}
