import { API_BASE } from '@/config/api'

export const getLearningProgress = async () => {
  const response = await fetch(`${API_BASE}/progress`)
  return response.json()
}

export const saveLearningProgress = async (chapterKey, progress, completed, detailJson = null, durationSeconds = 0) => {
  const response = await fetch(`${API_BASE}/progress`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ chapterKey, progress, completed, detailJson, durationSeconds })
  })
  return response.json()
}

export const resetLearningProgress = async () => {
  const response = await fetch(`${API_BASE}/progress`, { method: 'DELETE' })
  return response.json()
}
