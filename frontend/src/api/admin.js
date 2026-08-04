import http from './http'

export async function fetchAdminGames() {
  const { data } = await http.get('/api/admin/games')
  return data.data
}

export async function createGame(formData) {
  const { data } = await http.post('/api/admin/games', formData)
  return data.data
}

export async function updateGame(id, formData) {
  const { data } = await http.put(`/api/admin/games/${id}`, formData)
  return data.data
}

export async function deleteGame(id) {
  const { data } = await http.delete(`/api/admin/games/${id}`)
  return data.data
}
