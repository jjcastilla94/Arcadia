import http from './http'

export async function fetchCategories() {
  const { data } = await http.get('/api/categories')
  return data.data
}

export async function fetchGames({ search, category } = {}) {
  const { data } = await http.get('/api/games', {
    params: {
      search: search || undefined,
      category: category || undefined
    }
  })
  return data.data
}
