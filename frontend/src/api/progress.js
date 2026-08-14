import http from './http'

export async function saveProgress(gameId, data) {
  const { data: response } = await http.post('/progress/save', { gameId, data })
  return response.data
}

export async function fetchProgress(gameId) {
  const { data } = await http.get(`/progress/${gameId}`)
  return data.data
}
