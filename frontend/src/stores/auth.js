import { defineStore } from 'pinia'
import http from '../api/http'

const STORAGE_KEY = 'arcadia_auth'

function loadStored() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY)) || {}
  } catch {
    return {}
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => {
    const stored = loadStored()
    return {
      accessToken: stored.accessToken || '',
      refreshToken: stored.refreshToken || '',
      user: stored.user || null,
      initialized: false
    }
  },

  getters: {
    isAuthenticated: (state) => Boolean(state.accessToken),
    roleNames: (state) => state.user?.roles || []
  },

  actions: {
    persist() {
      localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify({
          accessToken: this.accessToken,
          refreshToken: this.refreshToken,
          user: this.user
        })
      )
    },

    setSession({ accessToken, refreshToken, user }) {
      this.accessToken = accessToken
      this.refreshToken = refreshToken
      this.user = user
      this.persist()
    },

    async initialize() {
      if (!this.accessToken) {
        this.initialized = true
        return
      }
      try {
        const res = await http.get('/users/me')
        this.user = res.data.data
        this.persist()
      } catch {
        this.logout()
      } finally {
        this.initialized = true
      }
    },

    async login(credentials) {
      const res = await http.post('/auth/login', credentials)
      this.setSession(res.data.data)
      return res.data.data
    },

    async register(payload) {
      const res = await http.post('/auth/register', payload)
      this.setSession(res.data.data)
      return res.data.data
    },

    setUser(user) {
      this.user = user
      this.persist()
    },

    logout() {
      this.accessToken = ''
      this.refreshToken = ''
      this.user = null
      this.initialized = true
      localStorage.removeItem(STORAGE_KEY)
    }
  }
})
