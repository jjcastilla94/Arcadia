import http from './http'

export async function fetchLibrary() {
  const { data } = await http.get('/library/my-games')
  return data.data
}

export async function isInLibrary(gameId) {
  const { data } = await http.get(`/library/${gameId}`)
  return data.data
}

export async function addToLibrary(gameId) {
  const { data } = await http.post(`/library/add/${gameId}`)
  return data.data
}

export async function removeFromLibrary(gameId) {
  const { data } = await http.delete(`/library/remove/${gameId}`)
  return data.data
}

export async function setLibraryStatus(gameId, status) {
  const { data } = await http.put(`/library/${gameId}/status`, { status })
  return data.data
}

export async function setLibraryRating(gameId, rating) {
  const { data } = await http.put(`/library/${gameId}/rating`, { rating })
  return data.data
}
