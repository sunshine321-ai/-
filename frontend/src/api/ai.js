import { API_BASE } from '@/config/api'

export const askAi = async (message, context = 'study', systemPrompt = '') => {
  try {
    const response = await fetch(`${API_BASE}/ai/chat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message, context, systemPrompt })
    })
    const data = await response.json()
    return response.ok && data.success
      ? { success: true, data: data.data.message }
      : { success: false, error: data.msg || 'AI 请求失败' }
  } catch (error) {
    return { success: false, error: error.message }
  }
}
