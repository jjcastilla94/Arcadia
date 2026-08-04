import http from './http'

export async function fetchCategories() {
  const { data } = await http.get('/categories')
  return data.data
}

export async function fetchGames({ search, category } = {}) {
  const { data } = await http.get('/games', {
    params: {
      search: search || undefined,
      category: category || undefined
    }
  })
  return data.data
}

export async function fetchGame(slug) {
  const { data } = await http.get(`/games/${slug}`)
  return data.data
}
