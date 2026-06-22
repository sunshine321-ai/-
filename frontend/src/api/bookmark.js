import { API_BASE } from '@/config/api'

export const getBookmarks = async () => {
  const response = await fetch(`${API_BASE}/bookmarks`)
  return response.json()
}

export const createBookmark = async (videoTime, label) => {
  const response = await fetch(`${API_BASE}/bookmarks`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ videoTime, label })
  })
  return response.json()
}

export const deleteBookmark = async (id) => {
  const response = await fetch(`${API_BASE}/bookmarks/${id}`, { method: 'DELETE' })
  return response.json()
}

export const clearBookmarks = async () => {
  const response = await fetch(`${API_BASE}/bookmarks`, { method: 'DELETE' })
  return response.json()
}
