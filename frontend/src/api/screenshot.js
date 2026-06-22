import { API_BASE } from '@/config/api'

const request = async (url, options) => {
  try {
    const response = await fetch(url, options)
    const data = await response.json()
    if (response.ok && data.success) return { success: true, data: data.data }
    return { success: false, error: data.msg || '请求失败' }
  } catch (error) {
    return { success: false, error: error.message }
  }
}

export const getScreenshots = () => request(`${API_BASE}/screenshots`)

export const createScreenshot = async (image, videoTime) => {
  const result = await request(`${API_BASE}/screenshots`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ image, videoTime })
  })
  if (!result.success) return result
  return {
    success: true,
    data: {
      id: result.data.id,
      url: result.data.imageUrl,
      videoTime: result.data.videoTime,
      createdAt: result.data.createdAt,
      updatedAt: result.data.updatedAt
    }
  }
}

export const updateScreenshot = (id, note, aiAnalysis) => request(`${API_BASE}/screenshots/${id}`, {
  method: 'PUT',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ note, aiAnalysis })
})

export const deleteScreenshot = (id) => request(`${API_BASE}/screenshots/${id}`, { method: 'DELETE' })

export const analyzeScreenshot = async (imagePath) => {
  const result = await request(`${API_BASE}/screenshots/analyze`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ imagePath })
  })
  return result.success ? { success: true, data: { content: result.data } } : result
}
