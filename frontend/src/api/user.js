import http from './http'

export const getMyProfile = () => http.get('/users/me')

export const updateProfile = (payload) => http.put('/users/me', payload)

export const changePassword = (payload) => http.put('/users/me/password', payload)
