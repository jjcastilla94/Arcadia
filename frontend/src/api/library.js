import http from './http'

export async function fetchLibrary() {
  const { data } = await http.get('/api/library/my-games')
  return data.data
}

export async function isInLibrary(gameId) {
  const { data } = await http.get(`/api/library/${gameId}`)
  return data.data
}

export async function addToLibrary(gameId) {
  const { data } = await http.post(`/api/library/add/${gameId}`)
  return data.data
}

export async function removeFromLibrary(gameId) {
  const { data } = await http.delete(`/api/library/remove/${gameId}`)
  return data.data
}

export async function setLibraryStatus(gameId, status) {
  const { data } = await http.put(`/api/library/${gameId}/status`, { status })
  return data.data
}

export async function setLibraryRating(gameId, rating) {
  const { data } = await http.put(`/api/library/${gameId}/rating`, { rating })
  return data.data
}
